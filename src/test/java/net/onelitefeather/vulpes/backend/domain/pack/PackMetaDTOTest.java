package net.onelitefeather.vulpes.backend.domain.pack;

import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for PackMetaDTO")
class PackMetaDTOTest extends ValidationTestBase<PackMetaDTO> {

    private static final String DESCRIPTION = "{\"text\":\"My pack\"}";

    private static PackMetaDTO range(int minMajor, int minMinor, int maxMajor, Integer maxMinor) {
        return new PackMetaDTO(DESCRIPTION, minMajor, minMinor, maxMajor, maxMinor, null, null);
    }

    @Test
    void acceptsValidDto() {
        assertNoViolation(new PackMetaDTO(DESCRIPTION, 48, 0, 57, null, Set.of("minecraft:trade_rebalance"), null), "description");
    }

    @Test
    void rejectsBlankDescription() {
        assertViolation(new PackMetaDTO(" ", 48, 0, 57, null, null, null), "description");
    }

    @Test
    void rejectsMinFormatMajorBelowOne() {
        assertViolation(range(0, 0, 57, null), "minFormatMajor");
    }

    @Test
    void rejectsNegativeMinFormatMinor() {
        assertViolation(range(48, -1, 57, null), "minFormatMinor");
    }

    @Test
    void rejectsMaxFormatMajorBelowOne() {
        assertViolation(range(1, 0, 0, null), "maxFormatMajor");
    }

    @Test
    void rejectsNegativeMaxFormatMinor() {
        assertViolation(range(48, 0, 57, -1), "maxFormatMinor");
    }

    @Test
    void formatRangeIsValidWhenMaxMajorIsAboveMinMajor() {
        assertTrue(range(48, 5, 57, 0).isFormatRangeValid());
    }

    @Test
    void formatRangeIsInvalidWhenMaxMajorIsBelowMinMajor() {
        assertFalse(range(57, 0, 48, null).isFormatRangeValid());
    }

    @Test
    void formatRangeIsValidForSameMajorWithoutMaxMinor() {
        assertTrue(range(57, 3, 57, null).isFormatRangeValid());
    }

    @Test
    void formatRangeIsValidForSameMajorWithEqualMinor() {
        assertTrue(range(57, 3, 57, 3).isFormatRangeValid());
    }

    @Test
    void formatRangeIsInvalidForSameMajorWithLowerMaxMinor() {
        assertFalse(range(57, 3, 57, 2).isFormatRangeValid());
    }

    @Test
    void featuresIsEmptyWhenNoneWereSent() {
        assertTrue(range(48, 0, 57, null).features().isEmpty());
    }

    @Test
    void toEntityCopiesAllValues() {
        PackMetaDTO dto = new PackMetaDTO(DESCRIPTION, 48, 1, 57, 2, Set.of("minecraft:bundle"), null);

        PackMetaEntity entity = dto.toEntity(null, PackType.DATA, null);

        assertNull(entity.getId());
        assertEquals(PackType.DATA, entity.getPackType());
        assertEquals(DESCRIPTION, entity.getDescription());
        assertEquals(48, entity.getMinFormatMajor());
        assertEquals(1, entity.getMinFormatMinor());
        assertEquals(57, entity.getMaxFormatMajor());
        assertEquals(2, entity.getMaxFormatMinor());
        assertEquals(Set.of("minecraft:bundle"), entity.getEnabledFeatures());
    }

    @Test
    void applyToOverwritesExistingValues() {
        PackMetaEntity entity = new PackMetaDTO("{\"text\":\"old\"}", 1, 0, 2, 0, Set.of("old"), null)
                .toEntity(null, PackType.RESOURCE, null);

        new PackMetaDTO(DESCRIPTION, 48, 1, 57, null, null, null).applyTo(entity, null);

        assertEquals(PackType.RESOURCE, entity.getPackType());
        assertEquals(DESCRIPTION, entity.getDescription());
        assertEquals(48, entity.getMinFormatMajor());
        assertEquals(1, entity.getMinFormatMinor());
        assertEquals(57, entity.getMaxFormatMajor());
        assertNull(entity.getMaxFormatMinor());
        assertTrue(entity.getEnabledFeatures().isEmpty());
    }
}
