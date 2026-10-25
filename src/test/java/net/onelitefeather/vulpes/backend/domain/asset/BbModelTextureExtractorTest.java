package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.backend.domain.asset.BbModelTextureExtractor.ExtractedTexture;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for BbModelTextureExtractor")
class BbModelTextureExtractorTest {

    @Test
    void extractsEmbeddedTextureWithKeyDerivedFromModelFolder() {
        String model = AssetFixtures.bbmodelWithTextures("Ruby Side.png");

        List<ExtractedTexture> textures = BbModelTextureExtractor.extract(
                "vulpes:item/ruby", AssetFixtures.utf8(model));

        assertEquals(1, textures.size(), "one embedded texture must be extracted");
        assertEquals("vulpes:item/ruby_side", textures.get(0).key(), "the key must keep the model folder");
        assertEquals("ruby_side.png", textures.get(0).fileName(), "the file name must be the sanitized name");
        assertEquals(16, textures.get(0).width(), "the width must be read from the embedded PNG");
    }

    @Test
    void sanitizesInvalidCharactersInTextureName() {
        String model = AssetFixtures.bbmodelWithTextures("My Texture!.PNG");

        List<ExtractedTexture> textures = BbModelTextureExtractor.extract(
                "vulpes:item/ruby", AssetFixtures.utf8(model));

        assertEquals("vulpes:item/my_texture_", textures.get(0).key(), "invalid characters must become underscores");
    }

    @Test
    void keyWithoutFolderUsesNamespaceAndName() {
        String model = AssetFixtures.bbmodelWithTextures("gem.png");

        List<ExtractedTexture> textures = BbModelTextureExtractor.extract(
                "vulpes:ruby", AssetFixtures.utf8(model));

        assertEquals("vulpes:gem", textures.get(0).key(), "without a folder the key is namespace plus name");
    }

    @Test
    void invalidCharactersAreReplacedOneByOne() {
        String model = AssetFixtures.bbmodelWithTextures("!!!");

        List<ExtractedTexture> textures = BbModelTextureExtractor.extract(
                "vulpes:item/ruby", AssetFixtures.utf8(model));

        assertEquals("vulpes:item/___", textures.get(0).key(), "each invalid character must become one underscore");
    }

    @Test
    void ignoresSourcesThatAreNotDataUris() {
        String model = "{\"textures\":[{\"name\":\"ext\",\"source\":\"textures/ext.png\"}]}";

        List<ExtractedTexture> textures = BbModelTextureExtractor.extract(
                "vulpes:item/ruby", AssetFixtures.utf8(model));

        assertTrue(textures.isEmpty(), "a texture referenced by path must not be extracted");
    }

    @Test
    void invalidBase64IsRejectedAsInvalidContent() {
        String model = "{\"textures\":[{\"name\":\"bad\",\"source\":\"data:image/png;base64,%%%not-base64%%%\"}]}";

        ApiException exception = assertThrows(
                ApiException.class,
                () -> BbModelTextureExtractor.extract("vulpes:item/ruby", AssetFixtures.utf8(model)),
                "an embedded texture that is not base64 must be rejected"
        );

        assertEquals("ASSET_INVALID_CONTENT", exception.violations().get(0).code(), "the code must be ASSET_INVALID_CONTENT");
    }

    @Test
    void embeddedBytesThatAreNotPngAreRejectedAsInvalidContent() {
        String notPng = Base64.getEncoder().encodeToString(AssetFixtures.utf8("not a png"));
        String model = "{\"textures\":[{\"name\":\"bad\",\"source\":\"data:image/png;base64," + notPng + "\"}]}";

        ApiException exception = assertThrows(
                ApiException.class,
                () -> BbModelTextureExtractor.extract("vulpes:item/ruby", AssetFixtures.utf8(model)),
                "an embedded texture that is not a PNG must be rejected"
        );

        assertEquals("ASSET_INVALID_CONTENT", exception.violations().get(0).code(), "the code must be ASSET_INVALID_CONTENT");
    }
}
