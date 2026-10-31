package net.onelitefeather.vulpes.backend.domain.asset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.jspecify.annotations.Nullable;

/**
 * Decides the {@link AssetKind} of an upload from its file extension and its content, and validates
 * the content.
 *
 * <ul>
 *   <li>{@code .png} is a {@link AssetKind#TEXTURE}. It must start with the PNG signature and decode.</li>
 *   <li>{@code .json} is a {@link AssetKind#MODEL} and {@code .bbmodel} a {@link AssetKind#BBMODEL}.
 *       Both must be JSON objects.</li>
 *   <li>Any other extension is rejected with {@code ASSET_UNSUPPORTED_TYPE}, broken content with
 *       {@code ASSET_INVALID_CONTENT}. Both are reported on field {@code file}.</li>
 * </ul>
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class AssetInspector {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', (byte) 0x1A, '\n'};
    private static final ObjectMapper JSON = new ObjectMapper();

    private AssetInspector() {}

    /**
     * The result of a successful inspection.
     *
     * @param kind        the kind of the asset
     * @param width       the width in pixels, only for textures, otherwise {@code null}
     * @param height      the height in pixels, only for textures, otherwise {@code null}
     */
    public record Inspection(
            AssetKind kind,
            @Nullable Integer width,
            @Nullable Integer height) {}

    /**
     * The dimensions of a decoded PNG.
     *
     * @param width  the width in pixels
     * @param height the height in pixels
     */
    public record PngDimensions(int width, int height) {}

    /**
     * Inspects an upload.
     *
     * @param fileName the file name as the client sent it, may be {@code null}
     * @param content  the uploaded bytes
     * @return the kind and, for textures, the dimensions
     * @throws ApiException {@code VALIDATION_FAILED} with a violation on field {@code file}
     */
    public static Inspection inspect(String fileName, byte[] content) {
        String extension = extensionOf(fileName);
        return switch (extension) {
            case "png" -> {
                PngDimensions dimensions = readPng(content, "file");
                yield new Inspection(AssetKind.TEXTURE, dimensions.width(), dimensions.height());
            }
            case "json" -> {
                readJsonObject(content, "file");
                yield new Inspection(AssetKind.MODEL, null, null);
            }
            case "bbmodel" -> {
                readJsonObject(content, "file");
                yield new Inspection(AssetKind.BBMODEL, null, null);
            }
            default ->
                throw fileViolation(AssetViolationCode.UNSUPPORTED_TYPE, "must be a .png, .json or .bbmodel file");
        };
    }

    /**
     * Decodes a PNG and returns its dimensions. Content that only looks like a PNG by its header is
     * rejected as well, because the decoder has to succeed.
     *
     * @param content the bytes to decode
     * @param field   the field to name in the violation
     * @return the dimensions of the image
     * @throws ApiException {@code VALIDATION_FAILED} with {@code ASSET_INVALID_CONTENT} on the field
     */
    public static PngDimensions readPng(byte[] content, String field) {
        if (content == null
                || content.length < PNG_SIGNATURE.length
                || !Arrays.equals(content, 0, PNG_SIGNATURE.length, PNG_SIGNATURE, 0, PNG_SIGNATURE.length)) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, "must be a valid PNG image");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw violation(field, AssetViolationCode.INVALID_CONTENT, "must be a valid PNG image");
            }
            return new PngDimensions(image.getWidth(), image.getHeight());
        } catch (IOException exception) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, "must be a valid PNG image");
        }
    }

    /**
     * Parses JSON and requires an object at the root.
     *
     * @param content the bytes to parse
     * @param field   the field to name in the violation
     * @return the root node
     * @throws ApiException {@code VALIDATION_FAILED} with {@code ASSET_INVALID_CONTENT} on the field
     */
    public static JsonNode readJsonObject(byte[] content, String field) {
        try {
            JsonNode root = JSON.readTree(content);
            if (root == null || !root.isObject()) {
                throw violation(field, AssetViolationCode.INVALID_CONTENT, "must be a JSON object");
            }
            return root;
        } catch (IOException exception) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, "must be a JSON object");
        }
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static ApiException fileViolation(String code, String message) {
        return violation("file", code, message);
    }

    private static ApiException violation(String field, String code, String message) {
        return ApiException.validationFailed(List.of(new ProblemDetail.Violation(field, code, message)));
    }
}
