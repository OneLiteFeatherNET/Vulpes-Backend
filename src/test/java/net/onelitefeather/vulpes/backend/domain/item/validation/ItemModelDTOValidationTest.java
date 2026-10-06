package net.onelitefeather.vulpes.backend.domain.item.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * The constraints of {@link ItemModelDTO} only apply in the {@code Create} and {@code Update} groups, so the
 * default group accepts blank values.
 */
@DisplayName("Validation of ItemModelDTO in the default group")
class ItemModelDTOValidationTest extends ValidationTestBase<ItemModelDTO> {

    @Test
    @DisplayName("a blank uiName is accepted")
    void blankUiName_isAccepted() {
        ItemModelDTO dto = new ItemModelDTO(UUID.randomUUID(), "", "key", "Some comment", "weapon");

        assertNoViolation(dto, "uiName");
    }

    @Test
    @DisplayName("a blank key is accepted")
    void blankKey_isAccepted() {
        ItemModelDTO dto = new ItemModelDTO(UUID.randomUUID(), "UI Name", "", "Some comment", "misc");

        assertNoViolation(dto, "key");
    }

    @Test
    @DisplayName("a blank comment is accepted")
    void blankComment_isAccepted() {
        ItemModelDTO dto = new ItemModelDTO(UUID.randomUUID(), "UI Name", "key", "", "misc");

        assertNoViolation(dto, "comment");
    }

    @Test
    @DisplayName("a blank groupName is accepted")
    void blankGroupName_isAccepted() {
        ItemModelDTO dto = new ItemModelDTO(UUID.randomUUID(), "UI Name", "key", "Some comment", "");

        assertNoViolation(dto, "groupName");
    }
}
