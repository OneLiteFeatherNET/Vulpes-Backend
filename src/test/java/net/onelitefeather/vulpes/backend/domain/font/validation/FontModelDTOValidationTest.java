package net.onelitefeather.vulpes.backend.domain.font.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class FontModelDTOValidationTest extends ValidationTestBase<FontModelDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "", // invalid
                "key",
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testBlankVariableNameNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "", // invalid
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertNoViolation(dto, "key");
    }

    @Test
    void testBlankProviderValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "", // invalid
                "mapper",
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertViolation(dto, "provider", Create.class);
    }

    @Test
    void testMissingProviderValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                null, // invalid
                "mapper",
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertViolation(dto, "provider", Update.class);
    }

    @Test
    void testBlankMapperNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "provider",
                "", // invalid
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertNoViolation(dto, "mapper");
    }

    @Test
    void testBlankTexturePathNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "provider",
                "mapper",
                "", // invalid
                "Some comment",
                10,
                20
        );

        assertNoViolation(dto, "texturePath");
    }

    @Test
    void testNegativeAscentNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "bitmap",
                null,
                null,
                "Some comment",
                -8, // shifts the glyphs down
                20
        );

        assertNoViolation(dto, "ascent", Update.class);
    }

    @Test
    void testNegativeHeightValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                10,
                -5
        );

        assertViolation(dto, "height", Create.class);
    }

    @Test
    void testZeroHeightValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                10,
                0 // invalid
        );

        assertViolation(dto, "height", Update.class);
    }
}
