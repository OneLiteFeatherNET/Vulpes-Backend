package net.onelitefeather.vulpes.backend.domain.asset;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Builds asset bytes in memory, so the tests need no files on disk.
 */
public final class AssetFixtures {

    private AssetFixtures() {}

    /**
     * Encodes a blank image of the given size as PNG.
     */
    public static byte[] png(int width, int height) {
        return encode(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png");
    }

    /**
     * Encodes a blank image of the given size as JPEG, for a content mismatch test.
     */
    public static byte[] jpeg(int width, int height) {
        return encode(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpg");
    }

    /**
     * Builds a Blockbench model with one embedded texture per given name.
     */
    public static String bbmodelWithTextures(String... textureNames) {
        StringBuilder textures = new StringBuilder();
        for (int index = 0; index < textureNames.length; index++) {
            if (index > 0) {
                textures.append(',');
            }
            String source = "data:image/png;base64," + Base64.getEncoder().encodeToString(png(16, 16));
            textures.append("{\"name\":\"")
                    .append(textureNames[index])
                    .append("\",\"source\":\"")
                    .append(source)
                    .append("\"}");
        }
        return "{\"meta\":{\"format_version\":\"4.10\"},\"textures\":[" + textures + "],\"elements\":[]}";
    }

    /**
     * Returns the given text as UTF-8 bytes.
     */
    public static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] encode(BufferedImage image, String format) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, out);
            return out.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
