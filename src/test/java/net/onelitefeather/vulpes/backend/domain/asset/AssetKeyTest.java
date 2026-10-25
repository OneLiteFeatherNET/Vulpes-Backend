package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Unit tests for AssetKey")
class AssetKeyTest {

    @Test
    void validKeyWithFolderIsAccepted() {
        assertTrue(AssetKey.isValid("vulpes:item/ruby"), "a namespaced key with a folder must be valid");
    }

    @Test
    void validKeyWithoutFolderIsAccepted() {
        assertTrue(AssetKey.isValid("vulpes:ruby"), "a key without a folder must be valid");
    }

    @Test
    void uppercaseKeyIsRejected() {
        assertFalse(AssetKey.isValid("Vulpes:item/ruby"), "an uppercase namespace must be rejected");
    }

    @Test
    void keyWithFileExtensionIsRejected() {
        assertFalse(AssetKey.isValid("vulpes:item/ruby.png"), "a key must not carry a file extension");
    }

    @Test
    void keyWithoutNamespaceIsRejected() {
        assertFalse(AssetKey.isValid("item/ruby"), "a key without a namespace must be rejected");
    }

    @Test
    void nullKeyIsRejected() {
        assertFalse(AssetKey.isValid(null), "a missing key must be rejected");
    }

    @Test
    void requireValidNamesFieldKeyAndCode() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> AssetKey.requireValid("Vulpes:item/ruby"),
                "an invalid key must throw"
        );

        assertEquals("key", exception.violations().get(0).field(), "the violation must name the key field");
        assertEquals("ASSET_KEY_INVALID", exception.violations().get(0).code(), "the code must be ASSET_KEY_INVALID");
    }
}
