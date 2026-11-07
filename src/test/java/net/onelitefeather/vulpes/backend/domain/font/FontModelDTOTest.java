package net.onelitefeather.vulpes.backend.domain.font;

import net.onelitefeather.vulpes.api.model.FontEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit tests for the conversion of a FontModelDTO into a FontEntity")
class FontModelDTOTest {

    private static FontModelDTO font(String mapper) {
        return new FontModelDTO(UUID.randomUUID(), "Font", "icons", "bitmap", mapper, "texture.png", null, 7, 8, null);
    }

    @Test
    @DisplayName("the converted entity keeps the mapper of the request")
    void keepsMapper() {
        FontEntity entity = font("font_icons").toFontModel(null);

        assertEquals("font_icons", entity.getMapper());
    }

    @Test
    @DisplayName("the converted entity with texture asset keeps the mapper of the request")
    void keepsMapperWithTextureAsset() {
        FontEntity entity = font("font_icons").toFontModel(null, null);

        assertEquals("font_icons", entity.getMapper());
    }

    @Test
    @DisplayName("a request without mapper keeps the default mapper of the entity")
    void keepsDefaultMapperWithoutMapper() {
        String defaultMapper = new FontEntity().getMapper();

        assertEquals(defaultMapper, font(null).toFontModel(null).getMapper());
        assertEquals(defaultMapper, font(" ").toFontModel(null).getMapper());
    }
}
