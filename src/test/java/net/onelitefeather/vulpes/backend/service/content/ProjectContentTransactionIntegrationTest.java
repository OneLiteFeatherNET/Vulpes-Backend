package net.onelitefeather.vulpes.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Checks that the snapshot taken by {@link SnapshotTransaction#begin()} makes the transaction read-only
 * repeatable read on PostgreSQL.
 */
@MicronautTest
@DisplayName("Transaction snapshot of the project content export with Testcontainers")
@EnabledIfDockerAvailable
class ProjectContentTransactionIntegrationTest {

    @Inject
    SnapshotProbe probe;

    @Test
    void snapshotSetsRepeatableReadInsideTheTransaction() {
        SnapshotProbe.TransactionState state = probe.observe();

        assertTrue(state.active(), "the probe must run inside a transaction");
        assertEquals("repeatable read", state.isolation(), "PostgreSQL must report repeatable read after the snapshot");
    }

    @Test
    void snapshotSetsReadOnlyInsideTheTransaction() {
        SnapshotProbe.TransactionState state = probe.observe();

        assertEquals("on", state.readOnly(), "PostgreSQL must report a read-only transaction after the snapshot");
    }

    /**
     * A bean with the same read-only transaction as the export, which takes the snapshot first and then reports
     * the settings of the open transaction.
     */
    @Singleton
    public static class SnapshotProbe {

        private final EntityManager entityManager;
        private final SnapshotTransaction snapshotTransaction;

        @Inject
        public SnapshotProbe(EntityManager entityManager, SnapshotTransaction snapshotTransaction) {
            this.entityManager = entityManager;
            this.snapshotTransaction = snapshotTransaction;
        }

        @Transactional(readOnly = true)
        public TransactionState observe() {
            snapshotTransaction.begin();
            boolean active = entityManager.getTransaction().isActive();
            String isolation = (String) entityManager.createNativeQuery("SHOW transaction_isolation").getSingleResult();
            String readOnly = (String) entityManager.createNativeQuery("SHOW transaction_read_only").getSingleResult();
            return new TransactionState(active, isolation, readOnly);
        }

        /**
         * The settings reported by PostgreSQL inside the probe transaction.
         *
         * @param active    whether a JPA transaction is active when the probe runs
         * @param isolation the value of {@code transaction_isolation}
         * @param readOnly  the value of {@code transaction_read_only}
         */
        public record TransactionState(boolean active, String isolation, String readOnly) {
        }
    }
}
