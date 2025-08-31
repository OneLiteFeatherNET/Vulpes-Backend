package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;

import java.util.UUID;

@Schema(
        requiredProperties = {
        }
)
@Introspected
@Serdeable
public final class SoundFileSourceDTO {
    private final UUID id;
    private final String name;
    private final float volume;
    private final float pitch;
    private final int weight;
    private final boolean stream;
    private final int attenuationDistance;
    private final boolean preload;
    private final String type;


    public SoundFileSourceDTO(UUID id, String name, float volume, float pitch, int weight, boolean stream, int attenuationDistance, boolean preload, String type) {
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
        this.weight = weight;
        this.stream = stream;
        this.attenuationDistance = attenuationDistance;
        this.preload = preload;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isStream() {
        return stream;
    }

    public int getAttenuationDistance() {
        return attenuationDistance;
    }

    public boolean isPreload() {
        return preload;
    }

    public String getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public SoundFileSource toEntity() {
        return new SoundFileSource(id, name, volume, pitch, weight, stream, attenuationDistance, preload, type);
    }
}
