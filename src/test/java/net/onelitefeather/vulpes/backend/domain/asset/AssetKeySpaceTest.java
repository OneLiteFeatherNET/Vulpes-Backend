package net.onelitefeather.vulpes.backend.domain.asset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit tests for AssetKeySpace")
class AssetKeySpaceTest {

    @ParameterizedTest
    @EnumSource(AssetKind.class)
    void everyKindMapsToItsKeySpace(AssetKind kind) {
        Map<AssetKind, AssetKeySpace> expected = Map.of(
                AssetKind.TEXTURE, AssetKeySpace.TEXTURE,
                AssetKind.MODEL, AssetKeySpace.MODEL,
                AssetKind.BBMODEL, AssetKeySpace.MODEL);
        assertEquals(expected.get(kind), AssetKeySpace.of(kind),
                "the kind " + kind + " must be stored in the key space " + expected.get(kind));
    }
}
