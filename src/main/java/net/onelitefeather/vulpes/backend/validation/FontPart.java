package net.onelitefeather.vulpes.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires every character of a font line to lie in the Unicode Private Use Area, so a custom glyph never
 * replaces a regular character. All three areas are allowed: {@code U+E000–U+F8FF} and the planes 15 and 16.
 *
 * <p>{@code U+0000}, which a bitmap provider uses for empty cells of its grid, is rejected: PostgreSQL cannot
 * store it in a text column. The generator pads the rows when it writes the font.
 * {@code null} is valid, it is left to {@code @NotNull} or {@code @NotBlank}.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FontPartValidator.class)
public @interface FontPart {

    String message() default "must only contain characters of the Unicode Private Use Area";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
