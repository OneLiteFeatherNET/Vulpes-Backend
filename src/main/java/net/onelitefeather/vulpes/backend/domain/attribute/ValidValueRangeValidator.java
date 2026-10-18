package net.onelitefeather.vulpes.backend.domain.attribute;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks {@link ValidValueRange}, which requires minimum ≤ default ≤ maximum.
 * <p>
 * A plain Jakarta validator, so Micronaut at runtime and Hibernate Validator in the tests both run it.
 * </p>
 */
@Introspected
public class ValidValueRangeValidator implements ConstraintValidator<ValidValueRange, AttributeModelDTO> {

    @Override
    public boolean isValid(AttributeModelDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.minimumValue() <= value.defaultValue() && value.defaultValue() <= value.maximumValue();
    }
}
