package net.onelitefeather.vulpes.backend.controller.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EntityTagMatcherTest {

    private static final String REVISION = "a3f1c9";

    @Test
    void wildcardMatchesAnyRevision() {
        assertTrue(EntityTagMatcher.matches("*", REVISION), "a wildcard If-None-Match must match");
    }

    @Test
    void quotedRevisionMatches() {
        assertTrue(EntityTagMatcher.matches("\"" + REVISION + "\"", REVISION), "the quoted ETag must match");
    }

    @Test
    void weakRevisionMatches() {
        assertTrue(EntityTagMatcher.matches("W/\"" + REVISION + "\"", REVISION), "the weak ETag must match");
    }

    @Test
    void listContainingMatchingRevisionMatches() {
        String header = "\"other\", W/\"" + REVISION + "\" , \"third\"";

        assertTrue(EntityTagMatcher.matches(header, REVISION), "a list containing the revision must match");
    }

    @Test
    void listWithoutMatchingRevisionDoesNotMatch() {
        String header = "\"other\", \"third\"";

        assertFalse(EntityTagMatcher.matches(header, REVISION), "a list without the revision must not match");
    }

    @Test
    void unquotedRevisionDoesNotMatch() {
        assertFalse(EntityTagMatcher.matches(REVISION, REVISION), "a bare revision without quotes is not an entity tag");
    }

    @Test
    void nullHeaderDoesNotMatch() {
        assertFalse(EntityTagMatcher.matches(null, REVISION), "a missing If-None-Match must not match");
    }

    @Test
    void blankHeaderDoesNotMatch() {
        assertFalse(EntityTagMatcher.matches("   ", REVISION), "a blank If-None-Match must not match");
    }

    @Test
    void staleRevisionDoesNotMatch() {
        assertFalse(EntityTagMatcher.matches("\"0000ff\"", REVISION), "a stale revision must not match");
    }
}
