package net.onelitefeather.vulpes.backend.validation;

/**
 * Lengths of the database columns that request values are stored in.
 */
public final class ColumnLength {

    /**
     * The length of a string column without an explicit length, which Hibernate creates as {@code varchar(255)}.
     * A longer value fails on insert, so the request DTOs reject it up front.
     */
    public static final int VARCHAR = 255;

    private ColumnLength() {
    }
}
