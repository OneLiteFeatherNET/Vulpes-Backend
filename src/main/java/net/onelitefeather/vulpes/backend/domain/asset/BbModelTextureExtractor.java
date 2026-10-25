package net.onelitefeather.vulpes.backend.domain.asset;

import com.fasterxml.jackson.databind.JsonNode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Extracts the textures embedded in a Blockbench model.
 *
 * <p>Every entry of {@code textures[]} whose {@code source} is a {@code data:image/png;base64,} URI
 * becomes one texture. Other sources are ignored. The key of a texture is derived from the key of the
 * model: the namespace and the folder of the model are kept, and the texture name is sanitized. For
 * example, the model {@code vulpes:item/ruby} with a texture named {@code Ruby Side.png} yields
 * {@code vulpes:item/ruby_side}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class BbModelTextureExtractor {

    private static final String DATA_URI_PREFIX = "data:image/png;base64,";

    private BbModelTextureExtractor() {
    }

    /**
     * A texture extracted from a Blockbench model.
     *
     * @param key     the derived asset key
     * @param fileName the file name, the sanitized texture name with {@code .png}
     * @param content the decoded PNG bytes
     * @param width   the width in pixels
     * @param height  the height in pixels
     */
    public record ExtractedTexture(String key, String fileName, byte[] content, int width, int height) {
    }

    /**
     * Extracts all embedded PNG textures of a Blockbench model.
     *
     * @param bbmodelKey the key of the model, used as the base of the texture keys
     * @param content    the raw {@code .bbmodel} bytes
     * @return the textures in the order they appear in the model, possibly empty
     * @throws ApiException {@code VALIDATION_FAILED} with {@code ASSET_INVALID_CONTENT} on field
     *                      {@code file} if the model or an embedded texture cannot be decoded
     */
    public static List<ExtractedTexture> extract(String bbmodelKey, byte[] content) {
        JsonNode root = AssetInspector.readJsonObject(content, "file");
        JsonNode textures = root.get("textures");
        List<ExtractedTexture> result = new ArrayList<>();
        if (textures == null || !textures.isArray()) {
            return result;
        }
        for (int index = 0; index < textures.size(); index++) {
            JsonNode texture = textures.get(index);
            JsonNode source = texture.get("source");
            if (source == null || !source.isTextual() || !source.asText().startsWith(DATA_URI_PREFIX)) {
                continue;
            }
            byte[] png = decodeBase64(source.asText().substring(DATA_URI_PREFIX.length()));
            AssetInspector.PngDimensions dimensions = AssetInspector.readPng(png, "file");
            String name = sanitizeName(textureName(texture), index);
            result.add(new ExtractedTexture(
                    deriveKey(bbmodelKey, name),
                    name + ".png",
                    png,
                    dimensions.width(),
                    dimensions.height()
            ));
        }
        return result;
    }

    /**
     * Derives the key of a texture from the key of its model and the sanitized texture name.
     *
     * @param bbmodelKey the key of the model, already validated
     * @param name       the sanitized texture name
     * @return the texture key
     */
    static String deriveKey(String bbmodelKey, String name) {
        int colon = bbmodelKey.indexOf(':');
        String namespace = bbmodelKey.substring(0, colon);
        String path = bbmodelKey.substring(colon + 1);
        int slash = path.lastIndexOf('/');
        if (slash < 0) {
            return namespace + ":" + name;
        }
        return namespace + ":" + path.substring(0, slash) + "/" + name;
    }

    /**
     * Strips the extension and replaces every character outside {@code [a-z0-9_-]} with an underscore.
     *
     * @param rawName the texture name as stored in the model, may be blank
     * @param index   the position of the texture, used when no usable name remains
     * @return the sanitized name
     */
    static String sanitizeName(String rawName, int index) {
        String name = rawName;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        name = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        return name.isEmpty() ? "texture_" + index : name;
    }

    private static String textureName(JsonNode texture) {
        JsonNode name = texture.get("name");
        return name != null && name.isTextual() ? name.asText() : "";
    }

    private static byte[] decodeBase64(String encoded) {
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException exception) {
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "file",
                    AssetViolationCode.INVALID_CONTENT,
                    "contains an embedded texture that is not valid base64"
            )));
        }
    }
}
