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
                "key",
                "Some comment",
                "weapon"
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
                "misc"
        );

        assertNoViolation(dto, "key");
    }

    @Test
    void testBlankCommentNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "", // valid
                "misc"
        );
        assertNoViolation(dto, "comment");
    }

    @Test
    void testBlankGroupNoValidation() {
        ItemModelDTO dto = new ItemModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
                "Some comment",
                "" // invalid
        );

        assertNoViolation(dto, "groupName");
    }
}
