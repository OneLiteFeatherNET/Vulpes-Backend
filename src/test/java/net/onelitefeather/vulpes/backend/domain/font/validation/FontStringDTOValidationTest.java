package net.onelitefeather.vulpes.backend.domain.font.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

class FontStringDTOValidationTest extends ValidationTestBase<FontStringDTO> {

    @Test
    void testBlankLineValidationFail() {
        FontStringDTO dto = new FontStringDTO(null, " ", 0);

        assertViolation(dto, "line", Create.class);
    }

    @Test
    void testNegativeOrderIndexValidationFail() {
        FontStringDTO dto = new FontStringDTO(UUID.randomUUID(), "\uE000", -1);

        assertViolation(dto, "orderIndex", Update.class);
    }

    @Test
    void testValidLineValidationPass() {
        FontStringDTO dto = new FontStringDTO(UUID.randomUUID(), "\uE000\uE001", 0);

        assertNoViolation(dto, "line", Update.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uE000", "\uF8FF", "\uDB80\uDC00", "\uDBBF\uDFFD", "\uDBC0\uDC00", "\uDBFF\uDFFD"})
    @DisplayName("A line of the three Private Use Areas passes")
    void fontPartLine(String line) {
        FontStringDTO dto = new FontStringDTO(null, line, 0);

        assertNoViolation(dto, "line", Create.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "\uE000A", "\uDFFF", "\u00E4", "\uD83D\uDE00", "\uDFFF\uE000", "\uE000\u0000\uE001"})
    @DisplayName("A line with a character outside the Private Use Areas, U+0000 included, fails")
    void outsideFontPart(String line) {
        FontStringDTO dto = new FontStringDTO(UUID.randomUUID(), line, 0);

        assertViolation(dto, "line", Update.class);
    }
}
