package net.onelitefeather.vulpes.backend.domain.advancement.validation;

import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class AdvancementModelDTOValidationTest extends ValidationTestBase<AdvancementModelDTO> {

    private static AdvancementModelDTO dto(String uiName, String key, String material,
                                           AdvancementFrameType frameType, String title) {
        return new AdvancementModelDTO(
                UUID.randomUUID(),
                uiName,
                key,
                "Some comment",
                material,
                frameType,
                title,
                null,
                null,
                null,
                0f,
                0f,
                true,
                true,
                false
        );
    }

    @Test
    void testBlankUiNameNoValidation() {
        assertNoViolation(dto("", "key", "minecraft:dirt", AdvancementFrameType.TASK, "\"Title\""), "uiName");
    }

    @Test
    void testBlankVariableNameNoValidation() {
        assertNoViolation(dto("UI Name", "", "minecraft:stone", AdvancementFrameType.TASK, "\"Title\""), "key");
    }

    @Test
    void testBlankMaterialNoValidation() {
        assertNoViolation(dto("UI Name", "key", "", AdvancementFrameType.TASK, "\"Title\""), "material");
    }

    @Test
    void testMissingFrameTypeNoValidation() {
        assertNoViolation(dto("UI Name", "key", "minecraft:stone", null, "\"Title\""), "frameType");
    }

    @Test
    void testBlankTitleNoValidation() {
        assertNoViolation(dto("UI Name", "key", "minecraft:stone", AdvancementFrameType.TASK, ""), "title");
    }
}
