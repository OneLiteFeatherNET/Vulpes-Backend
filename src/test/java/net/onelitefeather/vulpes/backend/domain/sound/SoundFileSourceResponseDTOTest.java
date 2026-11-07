package net.onelitefeather.vulpes.backend.domain.sound;

import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests for the response DTO of a sound file source")
class SoundFileSourceResponseDTOTest {

    @Test
    @DisplayName("createDTO() reads preload and stream from their own fields")
    void createDTOKeepsPreloadAndStreamApart() {
        SoundFileSource source = new SoundFileSource(
                UUID.randomUUID(), "ambient", 1f, 1f, 1, false, 16, true, "file");

        SoundResponseDTO.SoundFileSourceDTO dto = SoundResponseDTO.SoundFileSourceDTO.createDTO(source);

        assertFalse(dto.stream());
        assertTrue(dto.preload());
    }
}
