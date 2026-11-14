package net.onelitefeather.vulpes.backend.service.content;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

/**
 * Turns the current read-only transaction into a repeatable-read snapshot on PostgreSQL.
 *
 * <p>Under the default {@code READ COMMITTED} every statement sees the data committed when it starts, so a
 * transaction of several reads is not one consistent state. Under {@code REPEATABLE READ} all statements see
 * the state at the first read. The isolation is set with a statement instead of the Micronaut
 * {@code @Transactional(isolation = ...)} attribute, because Micronaut's {@code HibernateTransactionManager}
 * refuses custom isolation levels unless the Hibernate connection release mode is {@code on_close}, which the
 * project does not configure.
 *
 * <p>PostgreSQL accepts {@code SET TRANSACTION} only before the first query of a transaction, so {@link #begin()}
 * must be the first call inside the transaction. A pooled connection can carry a transaction that nobody started,
 * for example when the pool validated it while auto-commit was off; {@link #begin()} therefore rolls that one back
 * first, so the snapshot starts a fresh transaction.
 */
@Singleton
public class SnapshotTransaction {

    private static final String DISCARD_STALE = "ROLLBACK";
    private static final String SET_SNAPSHOT = "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ, READ ONLY";

    private final EntityManager entityManager;

    /**
     * Creates the snapshot helper.
     *
     * @param entityManager the entity manager bound to the current transaction
     */
    @Inject
    public SnapshotTransaction(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Sets the current transaction to read-only repeatable read. Must run inside a transaction and before any
     * other statement of that transaction. Rolls back whatever the connection still has open before, so callers
     * must not have done work in the same transaction.
     *
     * @throws IllegalStateException if no transaction is active
     */
    public void begin() {
        if (!entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("the snapshot must be taken inside a transaction");
        }
        entityManager.createNativeQuery(DISCARD_STALE).executeUpdate();
        entityManager.createNativeQuery(SET_SNAPSHOT).executeUpdate();
    }
}
