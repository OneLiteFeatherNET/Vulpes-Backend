package net.onelitefeather.vulpes.backend.domain.attribute.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class AttributeModelDTOValidationTest extends ValidationTestBase<AttributeModelDTO> {

    private static AttributeModelDTO attribute(UUID id, String uiName, String key, double defaultValue,
                                               double minimumValue, double maximumValue) {
        return new AttributeModelDTO(id, uiName, key, defaultValue, minimumValue, maximumValue, false, null, null);
    }

    @Test
    void testEmptyUiNameValidationFail() {
        AttributeModelDTO dto = attribute(UUID.randomUUID(), "", "empty_value", 1.0, 0.0, 5.0);

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testNegativeDefaultValueNoValidation() {
        // vanilla attributes like luck go below zero
        AttributeModelDTO dto = attribute(UUID.randomUUID(), "Luck", "luck", -1.0, -1.0, 5.0);

        assertNoViolation(dto, "defaultValue", Update.class);
    }

    @Test
    void testMaxValueValidationFail() {
        // must be strictly positive
        AttributeModelDTO dto = attribute(UUID.randomUUID(), "Speed", "playerSpeed", 0.0, 0.0, 0.0);

        assertViolation(dto, "maximumValue", Update.class);
    }

    @Test
    void testNegativeMaxValueValidationFail() {
        // must be strictly positive
        AttributeModelDTO dto = attribute(null, "Speed", "playerSpeed", -10.0, -10.0, -5.0);

        assertViolation(dto, "maximumValue", Create.class);
    }

    @Test
    void testEmptyVariableNameValidationFail() {
        AttributeModelDTO dto = attribute(UUID.randomUUID(), "Speed", "", 0.0, 0.0, 5.0);

        assertNoViolation(dto, "key");
    }

    // The value range is checked on the whole DTO, so its violation has an empty property path
    private static final String VALUE_RANGE = "";

    @Test
    void testDefaultBelowMinimumValidationFail() {
        AttributeModelDTO dto = attribute(null, "Luck", "luck", -1.0, 0.0, 5.0);

        assertViolation(dto, VALUE_RANGE, Create.class);
    }

    @Test
    void testDefaultAboveMaximumValidationFail() {
        AttributeModelDTO dto = attribute(UUID.randomUUID(), "Speed", "playerSpeed", 6.0, 0.0, 5.0);

        assertViolation(dto, VALUE_RANGE, Update.class);
    }

    @Test
    void testDefaultOnBoundsNoValidation() {
        assertNoViolation(attribute(null, "Speed", "playerSpeed", 0.0, 0.0, 5.0), VALUE_RANGE, Create.class);
        assertNoViolation(attribute(null, "Speed", "playerSpeed", 5.0, 0.0, 5.0), VALUE_RANGE, Create.class);
    }

    @Test
    void testSyncedAndTranslationKeyAreOptional() {
        AttributeModelDTO dto = new AttributeModelDTO(null, "Armor", "armor", 0.0, 0.0, 30.0, true,
                "attribute.name.armor", null);

        assertNoViolation(dto, "translationKey", Create.class);
    }
}
