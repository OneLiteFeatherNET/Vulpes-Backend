package net.onelitefeather.vulpes.backend.seed.data;

import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for the seed Filler and MinecraftCatalog")
class FillerTest {

    @Test
    @DisplayName("the same seed produces the same sequence")
    void sameSeed_sameSequence() {
        assertEquals(sample(new Filler(42)), sample(new Filler(42)));
    }

    @Test
    @DisplayName("a different seed produces a different sequence")
    void differentSeed_differentSequence() {
        assertNotEquals(sample(new Filler(42)), sample(new Filler(43)));
    }

    @Test
    @DisplayName("between() stays within its inclusive bounds")
    void between_isInclusive() {
        Filler filler = new Filler(7);
        for (int i = 0; i < 1_000; i++) {
            int value = filler.between(3, 5);
            assertTrue(value >= 3 && value <= 5, "out of bounds: " + value);
        }
    }

    @ParameterizedTest
    @EnumSource(EnvironmentAttributeKey.class)
    @DisplayName("every environment attribute key has a non-blank value")
    void environmentValue_isNeverBlank(EnvironmentAttributeKey key) {
        assertFalse(MinecraftCatalog.environmentValue(key).isBlank());
    }

    private static List<String> sample(Filler filler) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            values.add(filler.faker().ancient().hero());
            values.add(filler.faker().lorem().sentence());
            values.add(filler.pick(MinecraftCatalog.MATERIALS));
            values.add(String.valueOf(filler.between(0, 1_000)));
            values.add(String.valueOf(filler.chance(0.5)));
        }
        return values;
    }
}
