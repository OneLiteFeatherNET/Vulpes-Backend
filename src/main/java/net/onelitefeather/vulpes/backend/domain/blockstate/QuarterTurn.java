package net.onelitefeather.vulpes.backend.domain.blockstate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires a rotation of a blockstate model to be 0, 90, 180 or 270 degrees, the only ones Minecraft accepts.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuarterTurnValidator.class)
public @interface QuarterTurn {

    String message() default "must be 0, 90, 180 or 270";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
