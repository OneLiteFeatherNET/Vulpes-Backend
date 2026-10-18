package net.onelitefeather.vulpes.backend.domain.attribute;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires the default value of an attribute to lie between its minimum and its maximum value.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidValueRangeValidator.class)
public @interface ValidValueRange {

    String message() default "The default value must lie between the minimum and the maximum value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
