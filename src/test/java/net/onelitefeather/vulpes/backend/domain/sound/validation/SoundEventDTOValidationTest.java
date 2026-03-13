package net.onelitefeather.vulpes.backend.domain.sound.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SoundEventDTOValidationTest extends ValidationTestBase<SoundEventDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "", // invalid
                "variableName",
                "keyName",
                "SubTitle"
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testBlankVariableNameNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "UI Name",
                "", // invalid
                "keyName",
                "SubTitle"
        );

        assertNoViolation(dto, "variableName");
    }

    @Test
    void testBlankKeyNameNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "", // invalid
                "subTitle"
        );
        assertNoViolation(dto, "keyName");
    }

    @Test
    void testBlankSubTitleNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "UI Name",
                "variableName",
                "keyName",
                "" // invalid
        );

        assertNoViolation(dto, "subTitle");
    }
}
