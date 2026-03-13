package net.onelitefeather.vulpes.backend.domain.sound.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SoundFileSourceDTOValidationTest extends ValidationTestBase<SoundFileSourceDTO> {

    @Test
    void testBlankNameValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "",
                1.0f,
                1.0f,
                1,
                true,
                10,
                true,
                "typeA"
        );

        assertNoViolation(dto, "name");
    }

    @Test
    void testNegativeVolumeValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "soundName",
                -0.5f, // invalid
                1.0f,
                1,
                true,
                10,
                true,
                "typeA"
        );

        assertViolation(dto, "volume");
    }

    @Test
    void testNegativePitchValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "soundName",
                1.0f,
                -1.0f, // invalid
                1,
                true,
                10,
                true,
                "typeA"
        );

        assertViolation(dto, "pitch");
    }

    @Test
    void testNonPositiveWeightValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "soundName",
                1.0f,
                1.0f,
                0, // invalid
                true,
                10,
                true,
                "typeA"
        );

        assertViolation(dto, "weight");
    }

    @Test
    void testNonPositiveAttenuationDistanceValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "custom:scream",
                1.0f,
                2.0f,
                16,
                true,
                0, // invalid
                true,
                "typeA"
        );

        assertViolation(dto, "attenuationDistance");
    }

    @Test
    void testBlankTypeValidationFail() {
        SoundFileSourceDTO dto = new SoundFileSourceDTO(
                UUID.randomUUID(),
                "custom:growl",
                1.0f,
                1.0f,
                1,
                false,
                10,
                true,
                "" // invalid
        );
        assertNoViolation(dto, "type");
    }
}
