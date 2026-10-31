package net.onelitefeather.vulpes.backend.domain.asset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit tests for Sha1Hex")
class Sha1HexTest {

    @Test
    void digestOfAbcIsTheKnownSha1Vector() {
        assertEquals(
                "a9993e364706816aba3e25717850c26c9cd0d89d",
                Sha1Hex.of(AssetFixtures.utf8("abc")),
                "the SHA-1 of 'abc' must match the published test vector"
        );
    }
}
