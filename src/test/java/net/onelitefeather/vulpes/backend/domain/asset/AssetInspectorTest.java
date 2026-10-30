package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit tests for AssetInspector")
class AssetInspectorTest {

    @Test
    void pngIsInspectedAsTextureWithDimensions() {
        AssetInspector.Inspection inspection = AssetInspector.inspect("ruby.png", AssetFixtures.png(16, 32));

        assertEquals(AssetKind.TEXTURE, inspection.kind(), "a PNG upload must be a texture");
        assertEquals(16, inspection.width(), "the width must be read from the image");
        assertEquals(32, inspection.height(), "the height must be read from the image");
    }

    @Test
    void corruptPngIsRejectedAsInvalidContent() {
        byte[] truncated = Arrays.copyOf(AssetFixtures.png(16, 16), 40);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> AssetInspector.inspect("ruby.png", truncated),
                "a truncated PNG must not be accepted"
        );

        assertEquals("file", exception.violations().get(0).field(), "the violation must name the file field");
        assertEquals("ASSET_INVALID_CONTENT", exception.violations().get(0).code(), "the code must be ASSET_INVALID_CONTENT");
    }

    @Test
    void jpegContentWithPngExtensionIsRejectedAsInvalidContent() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> AssetInspector.inspect("ruby.png", AssetFixtures.jpeg(16, 16)),
                "a JPEG renamed to .png must not be accepted as a texture"
        );

        assertEquals("ASSET_INVALID_CONTENT", exception.violations().get(0).code(), "the code must be ASSET_INVALID_CONTENT");
    }

    @Test
    void jsonObjectIsInspectedAsModelWithoutDimensions() {
        AssetInspector.Inspection inspection = AssetInspector.inspect(
                "ruby.json", AssetFixtures.utf8("{\"parent\":\"minecraft:item/generated\"}"));

        assertEquals(AssetKind.MODEL, inspection.kind(), "a JSON object must be a model");
        assertNull(inspection.width(), "a model must not carry a width");
    }

    @Test
    void jsonArrayIsRejectedAsInvalidContent() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> AssetInspector.inspect("ruby.json", AssetFixtures.utf8("[1,2,3]")),
                "a JSON array is not a model"
        );

        assertEquals("ASSET_INVALID_CONTENT", exception.violations().get(0).code(), "the code must be ASSET_INVALID_CONTENT");
    }

    @Test
    void textFileIsRejectedAsUnsupportedType() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> AssetInspector.inspect("notes.txt", AssetFixtures.utf8("hello")),
                "a .txt upload must be rejected"
        );

        assertEquals("file", exception.violations().get(0).field(), "the violation must name the file field");
        assertEquals("ASSET_UNSUPPORTED_TYPE", exception.violations().get(0).code(), "the code must be ASSET_UNSUPPORTED_TYPE");
    }

    @Test
    void bbmodelIsInspectedAsBlockbenchModel() {
        AssetInspector.Inspection inspection = AssetInspector.inspect(
                "gem.bbmodel", AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("Gem")));

        assertEquals(AssetKind.BBMODEL, inspection.kind(), "a .bbmodel upload must be a Blockbench model");
    }
}
