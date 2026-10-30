package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Pageable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.service.FontService;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@DisplayName("Integration tests for the validation of the service method arguments")
@EnabledIfDockerAvailable
class ServiceMethodValidationIntegrationTest {

    @Inject
    FontService fontService;

    @Inject
    ItemService itemService;

    @Inject
    ProjectService projectService;

    private static void assertViolation(String path, Executable call) {
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, call);
        assertTrue(
                exception.getConstraintViolations().stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().endsWith(path)),
                () -> "expected a violation on " + path + " but got " + exception.getMessage()
        );
    }

    @Test
    @DisplayName("a missing id or body is rejected before the service runs")
    void nullArgumentsAreRejected() {
        assertViolation("id", () -> fontService.updateCharByFontId(null, new FontStringDTO(UUID.randomUUID(), "x", 0)));
        assertViolation("charModel", () -> fontService.updateCharByFontId(UUID.randomUUID(), null));
        assertViolation("pageable", () -> itemService.findLoreById(UUID.randomUUID(), null));
    }

    @Test
    @DisplayName("an update without the id of the entry is rejected with the update group")
    void updateRequiresTheId() {
        assertViolation(".id", () -> itemService.updateLoreById(UUID.randomUUID(), new ItemLoreDTO(null, "text")));
    }

    @Test
    @DisplayName("a create with an id is rejected with the create group")
    void createRejectsAnId() {
        assertViolation(".id", () -> itemService.createLoreById(UUID.randomUUID(), new ItemLoreDTO(UUID.randomUUID(), "text")));
    }

    @Test
    @DisplayName("the constraints of the crud service also hold for an overriding implementation")
    void overriddenCreateIsValidated() {
        assertViolation("dto.key", () -> projectService.create(new ProjectModelDTO(null, "Name", "", null, null, null, false)));
    }

    @Test
    @DisplayName("a negative lore index is rejected")
    void reorderRejectsANegativeIndex() {
        assertViolation("newIndex", () -> itemService.reorderLoreById(UUID.randomUUID(), UUID.randomUUID(), -1));
    }

    @Test
    @DisplayName("valid arguments reach the service")
    void validArgumentsPass() {
        assertTrue(itemService.findLoreById(UUID.randomUUID(), Pageable.UNPAGED).getContent().isEmpty());
    }
}
