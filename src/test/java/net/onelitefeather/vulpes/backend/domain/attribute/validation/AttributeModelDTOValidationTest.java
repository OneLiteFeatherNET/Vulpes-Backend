package net.onelitefeather.vulpes.backend.domain.attribute.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
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
                5.0
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testDefaultValueValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Speed",
                "playerSpeed",
                -1.0,
                5.0
        );

        assertViolation(dto, "defaultValue");
    }

    @Test
    void testMaxValueValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Speed",
                "playerSpeed",
                0.0,
                0.0 // must be strictly positive
        );

        assertViolation(dto, "maximumValue");
    }

    @Test
    void testEmptyVariableNameValidationFail() {
        AttributeModelDTO dto = new AttributeModelDTO(
                UUID.randomUUID(),
                "Speed",
                "",
                0.0,
                5.0
        );

        assertNoViolation(dto, "variableName");
    }
}
