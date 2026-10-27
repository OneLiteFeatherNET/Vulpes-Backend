package net.onelitefeather.vulpes.backend.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Unit tests for the object keys of asset bytes")
class AssetStorageKeyTest {

    private static final UUID PROJECT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ASSET = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final int NUMBER = 3;

    @Test
    void revisionKeyContainsProjectAssetAndRevisionNumber() {
        assertEquals(
                PROJECT + "/revisions/" + ASSET + "/" + NUMBER,
                AssetStorage.revisionKeyFor(PROJECT, ASSET, NUMBER));
    }

    @Test
    void revisionKeyDiffersFromLegacyKeyOfSameAsset() {
        assertNotEquals(
                AssetStorage.keyFor(PROJECT, ASSET),
                AssetStorage.revisionKeyFor(PROJECT, ASSET, NUMBER));
    }

    @Test
    void revisionKeyIsNotBelowLegacyKeyOfSameAsset() {
        // The local store cannot hold a file and a directory under the same path.
        String legacyKey = AssetStorage.keyFor(PROJECT, ASSET);
        assertFalse(AssetStorage.revisionKeyFor(PROJECT, ASSET, NUMBER).startsWith(legacyKey + "/"));
    }
}
