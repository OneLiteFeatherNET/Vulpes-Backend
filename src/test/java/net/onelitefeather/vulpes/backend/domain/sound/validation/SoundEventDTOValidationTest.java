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
                "key",
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

        assertNoViolation(dto, "key");
    }

    @Test
    void testBlankKeyNameNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "UI Name",
                "key",
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
                "key",
                "keyName",
                "" // invalid
        );

        assertNoViolation(dto, "subTitle");
    }
}
