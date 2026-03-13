package net.onelitefeather.vulpes.backend.domain.font.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class FontModelDTOValidationTest extends ValidationTestBase<FontModelDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "", // invalid
                "variableName",
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

        assertNoViolation(dto, "variableName");
    }

    @Test
    void testBlankProviderValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "", // invalid
                "mapper",
                "texturePath",
                "Some comment",
                10,
                20
        );

        assertViolation(dto, "provider");
    }

    @Test
    void testBlankMapperNoValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
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
                "variableName",
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
    void testNegativeAscentValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                -1, // invalid
                20
        );

        assertViolation(dto, "ascent");
    }

    @Test
    void testNegativeHeightValidation() {
        FontModelDTO dto = new FontModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "provider",
                "mapper",
                "texturePath",
                "Some comment",
                10,
                -5
        );

        assertViolation(dto, "height");
    }
}