package net.onelitefeather.vulpes.backend.service.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.json.tree.JsonNode;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContentRevisionTest {

    @Test
    void canonicalJsonSortsKeysAtNestedDepth() {
        Map<String, JsonNode> inner = new LinkedHashMap<>();
        inner.put("zeta", JsonNode.createNumberNode(1));
        inner.put("alpha", JsonNode.createNumberNode(2));
        Map<String, JsonNode> outer = new LinkedHashMap<>();
        outer.put("b", JsonNode.createObjectNode(inner));
        outer.put("a", JsonNode.createBooleanNode(true));
        JsonNode node = JsonNode.createObjectNode(outer);

        assertEquals(
                "{\"a\":true,\"b\":{\"alpha\":2,\"zeta\":1}}",
                ContentRevision.canonicalJson(node),
                "object keys must be sorted at every depth");
    }

    @Test
    void canonicalJsonContainsNoInsignificantWhitespace() {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("name", JsonNode.createStringNode("vulpes"));
        fields.put("list", JsonNode.createArrayNode(List.of(JsonNode.createNumberNode(1), JsonNode.createNumberNode(2))));
        JsonNode node = JsonNode.createObjectNode(fields);

        assertEquals(
                "{\"list\":[1,2],\"name\":\"vulpes\"}",
                ContentRevision.canonicalJson(node),
                "canonical JSON must not contain spaces or line breaks");
    }

    @Test
    void canonicalJsonKeepsArrayOrder() {
        JsonNode node = JsonNode.createArrayNode(List.of(
                JsonNode.createNumberNode(3), JsonNode.createNumberNode(1), JsonNode.createNumberNode(2)));

        assertEquals("[3,1,2]", ContentRevision.canonicalJson(node), "array element order must be preserved");
    }

    @Test
    void canonicalJsonEscapesQuoteAndBackslash() {
        JsonNode node = JsonNode.createStringNode("say \"hi\" \\ bye");

        assertEquals(
                "\"say \\\"hi\\\" \\\\ bye\"",
                ContentRevision.canonicalJson(node),
                "quote and backslash must be escaped");
    }

    @Test
    void canonicalJsonEscapesNewlineAsShortEscape() {
        JsonNode node = JsonNode.createStringNode("line1\nline2");

        assertEquals("\"line1\\nline2\"", ContentRevision.canonicalJson(node), "newline must be escaped as \\n");
    }

    @Test
    void canonicalJsonEscapesControlCharacterAsUnicodeEscape() {
        JsonNode node = JsonNode.createStringNode("\u0001");

        assertEquals("\"\\u0001\"", ContentRevision.canonicalJson(node), "control character must be escaped as \\u00XX");
    }

    @Test
    void canonicalJsonLeavesNonAsciiCharactersUnescaped() {
        JsonNode node = JsonNode.createStringNode("Füchse ✓");

        assertEquals("\"Füchse ✓\"", ContentRevision.canonicalJson(node), "non-ASCII characters must stay unescaped");
    }

    @Test
    void canonicalJsonRendersNullAndBooleans() {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("empty", JsonNode.nullNode());
        fields.put("off", JsonNode.createBooleanNode(false));
        fields.put("on", JsonNode.createBooleanNode(true));

        assertEquals(
                "{\"empty\":null,\"off\":false,\"on\":true}",
                ContentRevision.canonicalJson(JsonNode.createObjectNode(fields)),
                "null and boolean values must be rendered as JSON literals");
    }

    @Test
    void canonicalJsonRendersIntegralNumbersWithToString() {
        JsonNode node = JsonNode.createArrayNode(List.of(
                JsonNode.createNumberNode(42), JsonNode.createNumberNode(9_000_000_000L)));

        assertEquals("[42,9000000000]", ContentRevision.canonicalJson(node), "integral numbers must use toString");
    }

    @Test
    void canonicalJsonRendersDecimalWithoutTrailingZeros() {
        JsonNode node = JsonNode.createNumberNode(new BigDecimal("1.2500"));

        assertEquals("1.25", ContentRevision.canonicalJson(node), "BigDecimal must drop trailing zeros");
    }

    @Test
    void canonicalJsonRendersDoubleWithDoubleToString() {
        JsonNode node = JsonNode.createNumberNode(0.5d);

        assertEquals("0.5", ContentRevision.canonicalJson(node), "double must be rendered with Double.toString");
    }

    @Test
    void canonicalJsonRejectsNaN() {
        JsonNode node = JsonNode.createNumberNode(Double.NaN);

        assertThrows(
                IllegalArgumentException.class,
                () -> ContentRevision.canonicalJson(node),
                "NaN has no JSON representation and must be rejected");
    }

    @Test
    void canonicalJsonRejectsInfinity() {
        JsonNode node = JsonNode.createNumberNode(Double.POSITIVE_INFINITY);

        assertThrows(
                IllegalArgumentException.class,
                () -> ContentRevision.canonicalJson(node),
                "infinity has no JSON representation and must be rejected");
    }

    @Test
    void ofHashesCanonicalJsonOfObject() throws NoSuchAlgorithmException {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("a", JsonNode.createNumberNode(1));
        JsonNode body = JsonNode.createObjectNode(fields);

        assertEquals(
                sha256Hex("{\"a\":1}"),
                ContentRevision.of(body),
                "revision must be the SHA-256 of the canonical JSON");
    }

    @Test
    void ofExcludesTopLevelRevisionField() throws NoSuchAlgorithmException {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("a", JsonNode.createNumberNode(1));
        fields.put("revision", JsonNode.createStringNode("stale"));
        JsonNode body = JsonNode.createObjectNode(fields);

        assertEquals(
                sha256Hex("{\"a\":1}"),
                ContentRevision.of(body),
                "the top-level revision field must not contribute to the hash");
    }

    @Test
    void ofKeepsNestedRevisionField() throws NoSuchAlgorithmException {
        Map<String, JsonNode> nested = new LinkedHashMap<>();
        nested.put("revision", JsonNode.createNumberNode(7));
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("item", JsonNode.createObjectNode(nested));
        JsonNode body = JsonNode.createObjectNode(fields);

        assertEquals(
                sha256Hex("{\"item\":{\"revision\":7}}"),
                ContentRevision.of(body),
                "a nested key named revision must still contribute to the hash");
    }

    @Test
    void ofIsIndependentOfKeyInsertionOrder() {
        Map<String, JsonNode> forward = new LinkedHashMap<>();
        forward.put("first", JsonNode.createNumberNode(1));
        forward.put("second", JsonNode.createNumberNode(2));
        Map<String, JsonNode> backward = new LinkedHashMap<>();
        backward.put("second", JsonNode.createNumberNode(2));
        backward.put("first", JsonNode.createNumberNode(1));

        assertEquals(
                ContentRevision.of(JsonNode.createObjectNode(forward)),
                ContentRevision.of(JsonNode.createObjectNode(backward)),
                "the same content in a different key order must produce the same revision");
    }

    @Test
    void ofChangesWhenNestedValueChanges() {
        assertNotEquals(
                ContentRevision.of(bodyWithNestedValue(1)),
                ContentRevision.of(bodyWithNestedValue(2)),
                "a changed nested value must produce a different revision");
    }

    @Test
    void ofReturnsSixtyFourLowercaseHexCharacters() {
        String revision = ContentRevision.of(bodyWithNestedValue(1));

        assertEquals(64, revision.length(), "a SHA-256 hex digest has 64 characters");
        assertTrue(revision.matches("[0-9a-f]{64}"), "revision must be lowercase hex, was: " + revision);
    }

    @Test
    void ofRejectsNonObjectBody() {
        JsonNode body = JsonNode.createArrayNode(List.of(JsonNode.createNumberNode(1)));

        assertThrows(
                IllegalArgumentException.class,
                () -> ContentRevision.of(body),
                "only object bodies can carry a revision");
    }

    @Test
    void ofRejectsNonFiniteNumberInBody() {
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("broken", JsonNode.createNumberNode(Double.NaN));
        JsonNode body = JsonNode.createObjectNode(fields);

        assertThrows(
                IllegalArgumentException.class,
                () -> ContentRevision.of(body),
                "a body with a NaN value must be rejected rather than hashed");
    }

    private static JsonNode bodyWithNestedValue(int value) {
        Map<String, JsonNode> nested = new LinkedHashMap<>();
        nested.put("value", JsonNode.createNumberNode(value));
        Map<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("nested", JsonNode.createObjectNode(nested));
        return JsonNode.createObjectNode(fields);
    }

    private static String sha256Hex(String text) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
