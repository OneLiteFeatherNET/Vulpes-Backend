package net.onelitefeather.vulpes.backend.domain.expression.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

class ExpressionDTOValidationTest extends ValidationTestBase<ExpressionDTO> {

    @Test
    void testBlankUiNameOnCreate_violatesNotBlank() {
        ExpressionDTO dto = new ExpressionDTO(null, "", "a * 2", null, null, null, null, null);

        assertViolation(dto, "uiName", Create.class);
    }

    @Test
    void testBlankUiNameOnUpdate_violatesNotBlank() {
        ExpressionDTO dto = new ExpressionDTO(UUID.randomUUID(), " ", "a * 2", null, null, null, null, null);

        assertViolation(dto, "uiName", Update.class);
    }

    @Test
    void testIdOnCreate_violatesNull() {
        ExpressionDTO dto = new ExpressionDTO(UUID.randomUUID(), "Damage", "a * 2", null, null, null, null, null);

        assertViolation(dto, "id", Create.class);
    }

    @Test
    void testMissingIdOnUpdate_violatesNotNull() {
        ExpressionDTO dto = new ExpressionDTO(null, "Damage", "a * 2", null, null, null, null, null);

        assertViolation(dto, "id", Update.class);
    }

    @Test
    void testEmptyExpression_hasNoViolation() {
        ExpressionDTO dto = new ExpressionDTO(null, "Damage", "", null, null, null, null, null);

        assertNoViolation(dto, "expression", Create.class);
    }

    @Test
    void testValidCreate_hasNoViolationOnUiName() {
        ExpressionDTO dto = new ExpressionDTO(null, "Damage", "a * 2", null, null, null, null, null);

        assertNoViolation(dto, "uiName", Create.class);
    }
}
