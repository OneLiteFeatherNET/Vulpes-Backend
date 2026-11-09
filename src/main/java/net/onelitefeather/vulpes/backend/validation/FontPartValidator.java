package net.onelitefeather.vulpes.backend.validation;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks {@link FontPart}.
 * <p>
 * A plain Jakarta validator, so Micronaut at runtime and Hibernate Validator in the tests both run it.
 * </p>
 */
@Introspected
public class FontPartValidator implements ConstraintValidator<FontPart, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.codePoints().allMatch(codePoint -> Character.getType(codePoint) == Character.PRIVATE_USE);
    }
}
