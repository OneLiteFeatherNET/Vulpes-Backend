package net.onelitefeather.vulpes.backend.domain.dimension.validation;

import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class DimensionModelDTOValidationTest extends ValidationTestBase<DimensionModelDTO> {

    private static DimensionModelDTO sample(double coordinateScale, int minY, int height, int monsterSpawnBlockLightLimit) {
        return new DimensionModelDTO(
                UUID.randomUUID(),
                "Overworld",
                "overworld",
                false,
                true,
                false,
                false,
                coordinateScale,
                minY,
                height,
                384,
                "#minecraft:infiniburn_overworld",
                0.0f,
                "constant:11",
                monsterSpawnBlockLightLimit,
                Skybox.OVERWORLD,
                CardinalLight.DEFAULT,
                null
        );
    }

    @Test
    void testBlankUiNameNoValidation() {
        DimensionModelDTO dto = new DimensionModelDTO(
                UUID.randomUUID(),
                "", // invalid, but @NotBlank is scoped to Create/Update groups only
                "overworld",
                false,
                true,
                false,
                false,
                1.0,
                -64,
                384,
                384,
                "#minecraft:infiniburn_overworld",
                0.0f,
                "constant:11",
                15,
                Skybox.OVERWORLD,
                CardinalLight.DEFAULT,
                null
        );

        assertNoViolation(dto, "uiName");
    }

    @Test
    void testCoordinateScaleTooLowHasViolation() {
        DimensionModelDTO dto = sample(0.0000001, -64, 384, 15);

        assertViolation(dto, "coordinateScale");
    }

    @Test
    void testMinYOutOfRangeHasViolation() {
        DimensionModelDTO dto = sample(1.0, -3000, 384, 15);

        assertViolation(dto, "minY");
    }

    @Test
    void testHeightOutOfRangeHasViolation() {
        DimensionModelDTO dto = sample(1.0, -64, 5000, 15);

        assertViolation(dto, "height");
    }

    @Test
    void testMonsterSpawnBlockLightLimitOutOfRangeHasViolation() {
        DimensionModelDTO dto = sample(1.0, -64, 384, 20);

        assertViolation(dto, "monsterSpawnBlockLightLimit");
    }
}
