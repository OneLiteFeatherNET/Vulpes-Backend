package net.onelitefeather.vulpes.backend.service.content;

import io.micronaut.json.tree.JsonNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes the revision of a project's content. The revision is the SHA-256 of the canonical JSON of the
 * body without its top-level {@code revision} field, so equal content always yields the same ETag.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class ContentRevision {

    private static final String REVISION_FIELD = "revision";

    private ContentRevision() {}

    /**
     * Returns the canonical JSON of a node. Object keys are sorted by {@link String#compareTo} at every depth,
     * no insignificant whitespace is written, array order is kept, and strings are escaped as in RFC 8259.
     * Numbers are rendered deterministically: integral types with {@code toString()}, floating point with
     * {@link Double#toString(double)}, and {@link BigDecimal} without trailing zeros in plain notation.
     *
     * @param node the node to serialize
     * @return the canonical JSON text
     * @throws IllegalArgumentException if the node contains a NaN or infinite number, or an unsupported number type
     */
    public static String canonicalJson(JsonNode node) {
        StringBuilder builder = new StringBuilder();
        write(builder, node);
        return builder.toString();
    }

    /**
     * Returns the revision of a content body: the lowercase hex SHA-256 of the canonical JSON of the object
     * without its top-level {@code revision} field. Nested fields named {@code revision} still count.
     *
     * @param body the content body, which must be a JSON object
     * @return the revision as 64 lowercase hex characters
     * @throws IllegalArgumentException if the body is not an object or contains a non-finite number
     */
    public static String of(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new IllegalArgumentException("content body must be a JSON object");
        }
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, JsonNode> entry : sortedEntries(body)) {
            if (REVISION_FIELD.equals(entry.getKey())) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            writeString(builder, entry.getKey());
            builder.append(':');
            write(builder, entry.getValue());
        }
        builder.append('}');
        byte[] canonical = builder.toString().getBytes(StandardCharsets.UTF_8);
        return HexFormat.of().formatHex(sha256(canonical));
    }

    private static void write(StringBuilder builder, JsonNode node) {
        if (node.isNull()) {
            builder.append("null");
        } else if (node.isBoolean()) {
            builder.append(node.getBooleanValue() ? "true" : "false");
        } else if (node.isString()) {
            writeString(builder, node.getStringValue());
        } else if (node.isNumber()) {
            builder.append(formatNumber(node.getNumberValue()));
        } else if (node.isArray()) {
            builder.append('[');
            boolean first = true;
            for (JsonNode element : node.values()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                write(builder, element);
            }
            builder.append(']');
        } else if (node.isObject()) {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<String, JsonNode> entry : sortedEntries(node)) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                writeString(builder, entry.getKey());
                builder.append(':');
                write(builder, entry.getValue());
            }
            builder.append('}');
        } else {
            throw new IllegalArgumentException("unsupported JSON node: " + node);
        }
    }

    private static Iterable<Map.Entry<String, JsonNode>> sortedEntries(JsonNode object) {
        TreeMap<String, JsonNode> sorted = new TreeMap<>();
        for (Map.Entry<String, JsonNode> entry : object.entries()) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted.entrySet();
    }

    private static String formatNumber(Number value) {
        if (value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte
                || value instanceof BigInteger) {
            return value.toString();
        }
        if (value instanceof Double || value instanceof Float) {
            double number = value.doubleValue();
            if (Double.isNaN(number) || Double.isInfinite(number)) {
                throw new IllegalArgumentException("NaN and infinite numbers have no JSON representation");
            }
            return Double.toString(number);
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        throw new IllegalArgumentException("unsupported number type: " + value.getClass().getName());
    }

    private static void writeString(StringBuilder builder, String value) {
        builder.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        builder.append('"');
    }

    private static byte[] sha256(byte[] content) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(content);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", exception);
        }
    }
}
