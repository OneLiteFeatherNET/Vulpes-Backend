package net.onelitefeather.vulpes.backend.domain.item.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

class ItemModelDTOValidationTest extends ValidationTestBase<ItemModelDTO> {

    @Test
    void testBlankUiNameValidationFail() {
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

        assertViolation(dto, "uiName");
    }

    @Test
    void testBlankVariableNameValidationFail() {
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

        assertViolation(dto, "variableName");
    }

    @Test
    void testBlankCommentValidationFail() {
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
    void testBlankDisplayNameValidationFail() {
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

        assertViolation(dto, "displayName");
    }

    @Test
    void testBlankMaterialValidationFail() {
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

        assertViolation(dto, "material");
    }

    @Test
    void testBlankGroupValidationFail() {
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

        assertViolation(dto, "group");
    }

    @Test
    void testNegativeCustomModelDataValidationFail() {
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
    void testNegativeAmountValidationFail() {
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
