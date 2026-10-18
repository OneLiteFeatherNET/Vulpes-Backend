package net.onelitefeather.vulpes.backend.domain.item.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ItemEnchantmentDTOValidationTest extends ValidationTestBase<ItemEnchantmentDTO> {

    @Test
    void testZeroLevelValidationFail() {
        ItemEnchantmentDTO dto = new ItemEnchantmentDTO(null, "minecraft:sharpness", (short) 0, false);

        assertViolation(dto, "level", Create.class);
    }

    @Test
    void testNegativeLevelValidationFail() {
        ItemEnchantmentDTO dto = new ItemEnchantmentDTO(UUID.randomUUID(), "minecraft:sharpness", (short) -1, false);

        assertViolation(dto, "level", Update.class);
    }

    @Test
    void testPositiveLevelValidationPass() {
        ItemEnchantmentDTO dto = new ItemEnchantmentDTO(UUID.randomUUID(), "minecraft:sharpness", (short) 5, false);

        assertNoViolation(dto, "level", Update.class);
    }
}
