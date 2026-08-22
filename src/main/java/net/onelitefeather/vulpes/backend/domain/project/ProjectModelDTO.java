package net.onelitefeather.vulpes.backend.domain.project;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema
@Serdeable
@Introspected
public record ProjectModelDTO(
        @Schema(description = "ID of the project", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "Display name of the project", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String displayName,
        @Schema(description = "Unique key of the project", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Project URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String projectUrl,
        @Schema(description = "Documentation URL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String docuUrl,
        @Schema(description = "Description of the project", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description,
        @Schema(description = "Labor status of the project", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean labor
) {
    /**
     * Converts the DTO class to a {@link ProjectEntity}.
     *
     * @return the created entity
     */
    public @NotNull ProjectEntity toProjectEntity() {
        return new ProjectEntity(id, displayName, key, projectUrl, docuUrl, description, labor);
    }
}
