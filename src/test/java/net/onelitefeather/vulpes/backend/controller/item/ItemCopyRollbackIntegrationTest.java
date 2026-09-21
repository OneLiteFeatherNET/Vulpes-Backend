package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.data.model.Pageable;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.copier.AbstractRelationalModelCopier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves {@link AbstractRelationalModelCopier#copy}
 * is genuinely transactional, using {@link net.onelitefeather.vulpes.backend.service.impl.FailingItemModelCopier}
 * (active only in this environment) to force a failure after the root save has already happened.
 * If the transaction boundary works, the root copy never durably exists; if it doesn't, it does.
 *
 * <p><b>Fixture setup goes through the real HTTP API</b> (the same pattern
 * {@code SoundControllerIntegrationTest} already uses), never through direct repository writes
 * from the test thread: {@code @MicronautTest} wraps the whole test method in one uncommitted
 * transaction that rolls back at the end by default, so a direct {@code itemRepository.save(...)}
 * call from the test thread is invisible to the embedded server's own request-handling
 * thread/connection until that wrapping transaction commits &mdash; which it never does. An HTTP
 * call runs through the server's own request-scoped transaction, independent of the test method's
 * wrapper, so it commits normally and is safe to read back afterward through an injected
 * repository (only the direction "test-thread write, read from another thread before the test's
 * own transaction ends" is unsafe — reads made by the test thread after an HTTP call has already
 * returned are not).
 */
@MicronautTest(environments = "rollback-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration test proving AbstractRelationalModelCopier.copy() rolls back on failure")
@EnabledIfDockerAvailable
class ItemCopyRollbackIntegrationTest {

    @Inject
    EmbeddedServer server;

    @Inject
    ItemRepository itemRepository;

    private static final Faker FAKER = new Faker();

    private UUID projectId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "rollback-it-" + UUID.randomUUID(), null, null, null, false);
        ProjectModelResponseDTO.ProjectModelDTO created =
                given()
                        .contentType(ContentType.JSON)
                        .body(project)
                .when()
                        .post("/project")
                .then()
                        .statusCode(200)
                        .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        projectId = created.id();
    }

    @Test
    @DisplayName("a failure copying a relation rolls back the root save too")
    void copyFailure_rollsBackEverything() {
        ItemModelDTO sourceDto = new ItemModelDTO(
                null, "UI", "rollback-source", "comment", "display", "STONE", "group", 0, 1);
        ItemModelResponseDTO.ItemModelDTO source =
                given()
                        .contentType(ContentType.JSON)
                        .body(sourceDto)
                .when()
                        .post("/project/" + projectId + "/item")
                .then()
                        .statusCode(200)
                        .extract().as(ItemModelResponseDTO.ItemModelDTO.class);

        long itemCountBefore = itemRepository.findByProjectId(projectId, Pageable.unpaged()).getTotalSize();

        // targetKey must differ from the source's own key ("rollback-source") — a same-project
        // copy that reuses the source's key would 409 on the conflict check before ever reaching
        // copyRoot/copyRelation, and the simulated failure would never fire at all. relations only
        // needs to be non-empty: FailingItemModelCopier throws unconditionally on the first
        // relation it's asked to copy, regardless of whether the source actually has any lore,
        // flags, or enchantments — the point being proven is "root save + the relation loop are
        // one atomic unit," which a relation copy failing before it writes anything already
        // establishes just as well as one failing partway through would.
        given()
                .contentType(ContentType.JSON)
                .body("{\"targetKey\":\"rollback-copy\",\"relations\":[\"FLAGS\"]}")
        .when()
                .post("/project/" + projectId + "/item/" + source.id() + "/copy")
        .then()
                .statusCode(500);

        long itemCountAfter = itemRepository.findByProjectId(projectId, Pageable.unpaged()).getTotalSize();

        assertEquals(itemCountBefore, itemCountAfter,
                "the failed copy's root must not have been persisted — the transaction should have rolled it back");
    }
}
