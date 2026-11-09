package net.onelitefeather.vulpes.backend.domain.asset;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
     * Builds an Ogg Vorbis stream of two pages: the identification header, and a last page whose
     * granule position is the given sample count. A sample count of {@code -1} marks a last page on
     * which no packet ends, so the duration cannot be read.
     */
    public static byte[] oggVorbis(int channels, int sampleRate, long samples) {
        ByteBuffer header = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) 1).put("vorbis".getBytes(StandardCharsets.US_ASCII));
        header.putInt(0);
        header.put((byte) channels);
        header.putInt(sampleRate);
        header.putInt(0).putInt(128_000).putInt(0);
        header.put((byte) 0xB8);
        header.put((byte) 1);
        return concat(oggPage(0x02, 0, 0, header.array()), oggPage(0x04, samples, 1, new byte[] {0, 0, 0, 0}));
    }

    /**
     * Builds the first page of an Ogg Opus stream with one channel, which is not a Vorbis stream.
     */
    public static byte[] oggOpus() {
        ByteBuffer header = ByteBuffer.allocate(19).order(ByteOrder.LITTLE_ENDIAN);
        header.put("OpusHead".getBytes(StandardCharsets.US_ASCII));
        header.put((byte) 1)
                .put((byte) 1)
                .putShort((short) 312)
                .putInt(48_000)
                .putShort((short) 0)
                .put((byte) 0);
        return oggPage(0x02, 0, 0, header.array());
    }

    private static byte[] oggPage(int headerType, long granule, int sequence, byte[] packet) {
        ByteBuffer page = ByteBuffer.allocate(28 + packet.length).order(ByteOrder.LITTLE_ENDIAN);
        page.put("OggS".getBytes(StandardCharsets.US_ASCII));
        page.put((byte) 0);
        page.put((byte) headerType);
        page.putLong(granule);
        page.putInt(0x564C5550);
        page.putInt(sequence);
        page.putInt(0);
        page.put((byte) 1);
        page.put((byte) packet.length);
        page.put(packet);
        byte[] bytes = page.array();
        ByteBuffer.wrap(bytes, 22, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(oggCrc(bytes));
        return bytes;
    }

    private static int oggCrc(byte[] bytes) {
        int crc = 0;
        for (byte value : bytes) {
            crc ^= (value & 0xFF) << 24;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x80000000) != 0 ? (crc << 1) ^ 0x04C11DB7 : crc << 1;
            }
        }
        return crc;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
