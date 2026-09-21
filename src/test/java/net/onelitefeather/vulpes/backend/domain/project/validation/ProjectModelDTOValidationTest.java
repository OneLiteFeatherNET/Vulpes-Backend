package net.onelitefeather.vulpes.backend.domain.project.validation;

import jakarta.validation.ConstraintViolation;
import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectModelDTOValidationTest extends ValidationTestBase<ProjectModelDTO> {

    @Test
    void testBlankDisplayNameNoValidationDefaultGroup() {
        ProjectModelDTO dto = new ProjectModelDTO(
                UUID.randomUUID(),
                "",
                "test-key",
                "https://project.com",
                "https://docu.com",
                "Description",
                false
        );

        assertNoViolation(dto, "displayName");
    }

    @Test
    void testCreateGroupViolationWhenIdProvided() {
        ProjectModelDTO dto = new ProjectModelDTO(
                UUID.randomUUID(),
                "My Project",
                "my-project",
                null,
                null,
                null,
                false
        );

        Set<ConstraintViolation<ProjectModelDTO>> violations = validator.validate(dto, ValidationGroup.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void testCreateGroupViolationWhenDisplayNameBlank() {
        ProjectModelDTO dto = new ProjectModelDTO(
                null,
                "",
                "my-project",
                null,
                null,
                null,
                false
        );

        Set<ConstraintViolation<ProjectModelDTO>> violations = validator.validate(dto, ValidationGroup.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("displayName")));
    }

    @Test
    void testCreateGroupViolationWhenKeyBlank() {
        ProjectModelDTO dto = new ProjectModelDTO(
                null,
                "My Project",
                "",
                null,
                null,
                null,
                false
        );

        Set<ConstraintViolation<ProjectModelDTO>> violations = validator.validate(dto, ValidationGroup.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("key")));
    }

    @Test
    void testUpdateGroupViolationWhenIdNull() {
        ProjectModelDTO dto = new ProjectModelDTO(
                null,
                "My Project",
                "my-project",
                null,
                null,
                null,
                false
        );

        Set<ConstraintViolation<ProjectModelDTO>> violations = validator.validate(dto, ValidationGroup.Update.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void testCreateGroupValidDto() {
        ProjectModelDTO dto = new ProjectModelDTO(
                null,
                "My Project",
                "my-project",
                "https://example.com",
                "https://docs.example.com",
                "Project description",
                true
        );

        Set<ConstraintViolation<ProjectModelDTO>> violations = validator.validate(dto, ValidationGroup.Create.class);
        assertTrue(violations.isEmpty());
    }
}
