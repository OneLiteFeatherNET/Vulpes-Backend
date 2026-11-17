package net.onelitefeather.vulpes.backend.domain.blockstate;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks {@link QuarterTurn}.
 * <p>
 * A plain Jakarta validator, so Micronaut at runtime and Hibernate Validator in the tests both run it.
 * </p>
 */
@Introspected
public class QuarterTurnValidator implements ConstraintValidator<QuarterTurn, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || (value >= 0 && value < 360 && value % 90 == 0);
    }
}
