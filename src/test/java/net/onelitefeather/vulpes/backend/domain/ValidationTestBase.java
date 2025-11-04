package net.onelitefeather.vulpes.backend.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class ValidationTestBase<T> {

    protected static Validator validator;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * Helper method to validate a DTO and assert that it has a violation on a specific property
     *
     * @param dto          the DTO to validate
     * @param propertyName the name of the property to validate
     */
    protected void assertViolation(T dto, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected violations for property: " + propertyName);
        boolean found = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(propertyName));
        if (!found) {
            throw new AssertionError("No violation found for property: " + propertyName);
        }
    }

    /**
     * Helper method to validate a DTO and assert that it has no violations on a specific property
     *
     * @param dto          the DTO to validate
     * @param propertyName the name of the property to validate
     */
    protected void assertNoViolation(T dto, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected no violations for property: " + propertyName);
        boolean found = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(propertyName));
        if (found) {
            throw new AssertionError("Violation found for property: " + propertyName);
        }
    }
}
