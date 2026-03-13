package net.onelitefeather.vulpes.backend.domain.item.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ItemModelDTOValidationTest extends ValidationTestBase<ItemModelDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "", // invalid
                "variableName",
                "Some comment",
                "Display Name",
                "minecraft:gold_shovel",
                "weapon",
                1,
                1
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testBlankVariableNameNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "", // invalid
                "Some comment",
                "Display Name",
                "minecraft:dirt",
                "misc",
                1,
                1
        );

        assertNoViolation(dto, "variableName");
    }

    @Test
    void testBlankCommentNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "", // valid
                "Display Name",
                "minecraft:bucket",
                "misc",
                1,
                1
        );
        assertNoViolation(dto, "comment");
    }

    @Test
    void testBlankDisplayNameNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "", // invalid
                "minecraft:dirt",
                "misc",
                1,
                1
        );

        assertNoViolation(dto, "displayName");
    }

    @Test
    void testBlankMaterialNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Display Name",
                "", // invalid
                "weapon",
                1,
                1
        );

        assertNoViolation(dto, "material");
    }

    @Test
    void testBlankGroupNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Display Name",
                "minecraft:stone",
                "", // invalid
                1,
                1
        );

        assertNoViolation(dto, "groupName");
    }

    @Test
    void testNegativeCustomModelDataValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Display Name",
                "material:wool",
                "misc",
                -1, // invalid
                1
        );

        assertViolation(dto, "customModelData");
    }

    @Test
    void testNegativeAmountValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Display Name",
                "minecraft:dirt",
                "tools",
                1,
                -1
        );

        assertViolation(dto, "amount");
    }
}
