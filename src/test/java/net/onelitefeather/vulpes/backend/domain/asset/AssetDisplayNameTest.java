package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit tests for AssetDisplayName")
class AssetDisplayNameTest {

    @Test
    void validDisplayNameIsTrimmed() {
        assertEquals("Ruby Gem", AssetDisplayName.requireValid("  Ruby Gem "), "surrounding whitespace must be dropped");
    }

    @Test
    void displayNameOfColumnLengthIsAccepted() {
        String name = "a".repeat(AssetDisplayName.MAX_LENGTH);

        assertEquals(name, AssetDisplayName.requireValid(name), "a name as long as the column must be accepted");
    }

    @Test
    void missingBlankAndTooLongDisplayNamesNameFieldAndCode() {
        for (String invalid : new String[]{null, "", "   ", "a".repeat(AssetDisplayName.MAX_LENGTH + 1)}) {
            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> AssetDisplayName.requireValid(invalid),
                    "an invalid display name must throw"
            );

            assertEquals("displayName", exception.violations().get(0).field(), "the violation must name the displayName field");
            assertEquals("ASSET_DISPLAY_NAME_INVALID", exception.violations().get(0).code(),
                    "the code must be ASSET_DISPLAY_NAME_INVALID");
        }
    }
}
