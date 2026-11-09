package net.onelitefeather.vulpes.backend.domain.asset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
 *   <li>{@code .ogg} is a {@link AssetKind#SOUND}. It must be an Ogg Vorbis stream, because the client
 *       plays no other codec, and it must have one channel, because the client does not attenuate a
 *       sound with more channels by distance. Only the identification header and the last page are
 *       read; the audio itself is not decoded.</li>
 *   <li>Any other extension is rejected with {@code ASSET_UNSUPPORTED_TYPE}, broken content with
 *       {@code ASSET_INVALID_CONTENT} and a sound with more than one channel with
 *       {@code ASSET_SOUND_NOT_MONO}. All are reported on field {@code file}.</li>
 * </ul>
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class AssetInspector {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', (byte) 0x1A, '\n'};
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final byte[] OGG_CAPTURE = {'O', 'g', 'g', 'S'};
    private static final byte[] VORBIS_IDENTIFICATION = {1, 'v', 'o', 'r', 'b', 'i', 's'};
    private static final int OGG_PAGE_HEADER_LENGTH = 27;
    private static final int OGG_BEGINNING_OF_STREAM = 0x02;
    private static final int VORBIS_IDENTIFICATION_LENGTH = 30;
    private static final String NOT_VORBIS = "must be an Ogg Vorbis file";

    private AssetInspector() {}

    /**
     * The result of a successful inspection.
     *
     * @param kind        the kind of the asset
     * @param width       the width in pixels, only for textures, otherwise {@code null}
     * @param height      the height in pixels, only for textures, otherwise {@code null}
     * @param channels    the number of audio channels, only for sounds, otherwise {@code null}
     * @param sampleRate  the sample rate in Hz, only for sounds, otherwise {@code null}
     * @param durationMs  the duration in milliseconds, only for sounds whose last page carries a
     *                    granule position, otherwise {@code null}
     */
    public record Inspection(
            AssetKind kind,
            @Nullable Integer width,
            @Nullable Integer height,
            @Nullable Integer channels,
            @Nullable Integer sampleRate,
            @Nullable Long durationMs) {

        static Inspection of(AssetKind kind) {
            return new Inspection(kind, null, null, null, null, null);
        }
    }

    /**
     * The values read from an Ogg Vorbis stream.
     *
     * @param channels   the number of channels
     * @param sampleRate the sample rate in Hz
     * @param durationMs the duration in milliseconds, {@code null} if it cannot be read
     */
    public record VorbisInfo(
            int channels, int sampleRate, @Nullable Long durationMs) {}

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
                yield new Inspection(AssetKind.TEXTURE, dimensions.width(), dimensions.height(), null, null, null);
            }
            case "json" -> {
                readJsonObject(content, "file");
                yield Inspection.of(AssetKind.MODEL);
            }
            case "bbmodel" -> {
                readJsonObject(content, "file");
                yield Inspection.of(AssetKind.BBMODEL);
            }
            case "ogg" -> {
                VorbisInfo vorbis = readVorbis(content, "file");
                if (vorbis.channels() != 1) {
                    throw fileViolation(AssetViolationCode.SOUND_NOT_MONO, "must have exactly one channel");
                }
                yield new Inspection(
                        AssetKind.SOUND, null, null, vorbis.channels(), vorbis.sampleRate(), vorbis.durationMs());
            }
            default ->
                throw fileViolation(
                        AssetViolationCode.UNSUPPORTED_TYPE, "must be a .png, .json, .bbmodel or .ogg file");
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

    /**
     * Reads the identification header of an Ogg Vorbis stream, and the duration from the granule
     * position of its last page.
     *
     * <p>The first page must start the stream and carry the Vorbis identification header with version
     * 0, at least one channel, a sample rate above 0 and the framing bit set. Page checksums are not
     * verified.
     *
     * @param content the bytes to read
     * @param field   the field to name in the violation
     * @return the channels, the sample rate and, if it can be read, the duration
     * @throws ApiException {@code VALIDATION_FAILED} with {@code ASSET_INVALID_CONTENT} on the field
     */
    public static VorbisInfo readVorbis(byte[] content, String field) {
        if (content == null
                || content.length < OGG_PAGE_HEADER_LENGTH
                || !startsWith(content, 0, OGG_CAPTURE)
                || content[4] != 0
                || (content[5] & OGG_BEGINNING_OF_STREAM) == 0) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, NOT_VORBIS);
        }
        int segments = content[26] & 0xFF;
        int packet = OGG_PAGE_HEADER_LENGTH + segments;
        if (segments == 0
                || content.length < packet + VORBIS_IDENTIFICATION_LENGTH
                || !startsWith(content, packet, VORBIS_IDENTIFICATION)) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, NOT_VORBIS);
        }
        ByteBuffer header = ByteBuffer.wrap(content, packet, VORBIS_IDENTIFICATION_LENGTH)
                .slice()
                .order(ByteOrder.LITTLE_ENDIAN);
        int version = header.getInt(7);
        int channels = header.get(11) & 0xFF;
        long sampleRate = header.getInt(12) & 0xFFFFFFFFL;
        boolean framing = (header.get(29) & 0x01) != 0;
        if (version != 0 || channels == 0 || sampleRate == 0 || sampleRate > Integer.MAX_VALUE || !framing) {
            throw violation(field, AssetViolationCode.INVALID_CONTENT, NOT_VORBIS);
        }
        return new VorbisInfo(channels, (int) sampleRate, durationMs(content, sampleRate));
    }

    private static @Nullable Long durationMs(byte[] content, long sampleRate) {
        for (int start = content.length - OGG_PAGE_HEADER_LENGTH; start > 0; start--) {
            if (startsWith(content, start, OGG_CAPTURE) && content[start + 4] == 0) {
                long granule = ByteBuffer.wrap(content, start + 6, Long.BYTES)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .getLong();
                return granule < 0 ? null : granule * 1000 / sampleRate;
            }
        }
        return null;
    }

    private static boolean startsWith(byte[] content, int offset, byte[] prefix) {
        return content.length - offset >= prefix.length
                && Arrays.equals(content, offset, offset + prefix.length, prefix, 0, prefix.length);
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
