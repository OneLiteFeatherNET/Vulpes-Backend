package net.onelitefeather.vulpes.backend.domain.sound.validation;

import net.onelitefeather.vulpes.api.model.sound.SoundSource;
import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

class SoundEventDTOValidationTest extends ValidationTestBase<SoundEventDTO> {

    @Test
    void testBlankUiNameNoValidation() {
        SoundEventDTO dto = new SoundEventDTO(
                UUID.randomUUID(),
                "", // invalid
                "key",
                "keyName",
                "SubTitle",
                null
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
                "SubTitle",
                null
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
                "subTitle",
                null
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
                "", // invalid
                null
        );

        assertNoViolation(dto, "subTitle");
    }

    private static SoundEventDTO withSound(double minimumValue, SoundSource source) {
        return new SoundEventDTO(null, "UI Name", "key", "keyName", "subTitle", null, minimumValue, source);
    }

    @Test
    void minimumValueFromZeroToOneIsValid() {
        assertNoViolation(withSound(0.0, SoundSource.MASTER), "minimumValue", Create.class);
        assertNoViolation(withSound(1.0, SoundSource.MASTER), "minimumValue", Create.class);
    }

    @Test
    void minimumValueBelowZeroIsInvalid() {
        assertViolation(withSound(-0.1, SoundSource.MASTER), "minimumValue", Create.class);
    }

    @Test
    void minimumValueAboveOneIsInvalid() {
        assertViolation(withSound(1.1, SoundSource.MASTER), "minimumValue", Create.class);
    }

    @Test
    void missingSourceFallsBackToMaster() {
        assertEquals(SoundSource.MASTER, withSound(0.0, null).source());
    }
}
