package net.onelitefeather.vulpes.backend.domain.attribute.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class AttributeModelDTOValidationTest extends ValidationTestBase<AttributeModelDTO> {

    @Test
    void testEmptyUiNameValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "",
                "empty_value", // invalid
                1.0,
                5.0,
                null
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testNegativeDefaultValueNoValidation() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Luck",
                "luck",
                -1.0, // vanilla attributes like luck go below zero
                5.0,
                null
        );

        assertNoViolation(dto, "defaultValue", Update.class);
    }

    @Test
    void testMaxValueValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Speed",
                "playerSpeed",
                0.0,
                0.0, // must be strictly positive
                null
        );

        assertViolation(dto, "maximumValue", Update.class);
    }

    @Test
    void testNegativeMaxValueValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                null,
                "Speed",
                "playerSpeed",
                0.0,
                -5.0, // must be strictly positive
                null
        );

        assertViolation(dto, "maximumValue", Create.class);
    }

    @Test
    void testEmptyVariableNameValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Speed",
                "",
                0.0,
                5.0,
                null
        );

        assertNoViolation(dto, "key");
    }
}
