package net.onelitefeather.vulpes.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.context.annotation.Property;
import io.micronaut.jdbc.DataSourceResolver;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.TransactionState;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Reproduces the production pool: a single connection with auto-commit disabled by the pool and Hibernate told
 * that the provider disables auto-commit. A connection validated by the pool after it was idle can carry a
 * transaction that nobody started, so the first export after such an idle period must still begin its own
 * snapshot transaction.
 */
@MicronautTest(transactional = false)
@Property(name = "datasources.default.maximum-pool-size", value = "1")
@Property(name = "datasources.default.auto-commit", value = "false")
@Property(name = "jpa.default.properties.hibernate.connection.provider_disables_autocommit", value = "true")
@DisplayName("Content export after a stale pooled connection with Testcontainers")
@EnabledIfDockerAvailable
class ProjectContentStaleTransactionIntegrationTest {

    @Inject
    ProjectContentService contentService;

    @Inject
    ProjectService projectService;

    @Inject
    DataSource dataSource;

    @Inject
    DataSourceResolver dataSourceResolver;

    @Inject
    ProjectContentTransactionIntegrationTest.SnapshotProbe probe;

    @Test
    void exportSucceedsWhenThePooledConnectionCarriesAnOpenTransaction() throws SQLException {
        UUID projectId = createProject();
        leaveStaleTransactionOnPooledConnection();

        ProjectContentDTO content = assertDoesNotThrow(
                () -> contentService.export(projectId),
                "the export must start its own transaction even when the pooled connection carries a stale one");

        assertEquals(projectId, content.project().id(), "the export must return the requested project");
    }

    @Test
    void snapshotStillSetsRepeatableReadReadOnlyAfterAStaleConnection() throws SQLException {
        leaveStaleTransactionOnPooledConnection();

        ProjectContentTransactionIntegrationTest.SnapshotProbe.TransactionState state =
                assertDoesNotThrow(probe::observe,
                        "the snapshot must start its own transaction even when the pooled connection carries a stale one");

        assertEquals("repeatable read", state.isolation(),
                "the export snapshot must stay repeatable read after a stale pooled connection");
        assertEquals("on", state.readOnly(),
                "the export snapshot must stay read-only after a stale pooled connection");
    }

    /**
     * Borrows the only pooled connection, runs a query on the driver connection behind the pool proxy, so the pool
     * does not see the transaction this opens, and returns the connection to the pool with that transaction still
     * open. This is the state a pool validation leaves behind when auto-commit is off. The pool is reached past the
     * contextual data source, which only hands out the connection of a running transaction.
     */
    private void leaveStaleTransactionOnPooledConnection() throws SQLException {
        try (Connection connection = dataSourceResolver.resolve(dataSource).getConnection()) {
            BaseConnection driverConnection = connection.unwrap(BaseConnection.class);
            try (Statement statement = driverConnection.createStatement()) {
                statement.execute("SELECT 1");
            }
            assertEquals(TransactionState.OPEN, driverConnection.getTransactionState(),
                    "precondition: the pooled connection must carry an open transaction");
        }
    }

    private UUID createProject() {
        ProjectModelDTO dto = new ProjectModelDTO(
                null, "Stale content export", "stale-content-it-" + UUID.randomUUID(), null, null, null, false);
        ProjectModelResponseDTO.ProjectModelDTO created = projectService.create(dto);
        return created.id();
    }
}
