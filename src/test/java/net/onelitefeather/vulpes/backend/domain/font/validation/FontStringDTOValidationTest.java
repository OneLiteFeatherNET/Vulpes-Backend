package net.onelitefeather.vulpes.backend.domain.font.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.Test;

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
}
