package net.onelitefeather.vulpes.backend.domain.notification.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class NotificationModelDTOValidationTest  extends ValidationTestBase<NotificationModelDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        NotificationModelDTO dto = new NotificationModelDTO(
                UUID.randomUUID(),
                "", // invalid
                "variableName",
                "Some comment",
                "minecraft:dirt",
                "Task",
                "Notification Title"
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testBlankVariableNameNoValidation() {
        NotificationModelDTO dto = new NotificationModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "", // invalid
                "Some comment",
                "minecraft:stone",
                "FrameType",
                "Notification Title"
        );

        assertNoViolation(dto, "variableName");
    }

    @Test
    void testBlankMaterialNoValidation() {
        NotificationModelDTO dto = new NotificationModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "", // invalid
                "FrameTypeA",
                "Notification Title"
        );

        assertNoViolation(dto, "material");
    }

    @Test
    void testBlankFrameTypeNoValidation() {
        NotificationModelDTO dto = new NotificationModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Material1",
                "", // invalid
                "Notification Title"
        );

        assertNoViolation(dto, "frameType");
    }

    @Test
    void testBlankTitleNoValidation() {
        NotificationModelDTO dto = new NotificationModelDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "Some comment",
                "Material1",
                "FrameTypeA",
                "" // invalid
        );

        assertNoViolation(dto, "title");
    }
}
