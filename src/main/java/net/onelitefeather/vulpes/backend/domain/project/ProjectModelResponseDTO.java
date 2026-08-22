package net.onelitefeather.vulpes.backend.domain.project;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Project Model")
@Serdeable
public interface ProjectModelResponseDTO {

    /**
     * The {@link ProjectModelDTO} is used to represent a project model in the system.
     *
     * @param id          the unique identifier of the project model
     * @param displayName the display name of the project
     * @param key         the unique key of the project
     * @param projectUrl  the project URL
     * @param docuUrl     the documentation URL
     * @param description the description of the project
     * @param labor       whether the project is a labor project
     */
    @Schema(
            name = "ResponseProjectModelDTO",
            description = "Project Model Data"
    )
    @Serdeable
    record ProjectModelDTO(
            @Schema(description = "UUID of the Project Model") UUID id,
            @Schema(description = "Display name of the project") String displayName,
            @Schema(description = "Unique key of the project") String key,
            @Schema(description = "Project URL") String projectUrl,
            @Schema(description = "Documentation URL") String docuUrl,
            @Schema(description = "Description of the project") String description,
            @Schema(description = "Labor status of the project") boolean labor
    ) implements ProjectModelResponseDTO {

        /**
         * Creates a DTO from a ProjectEntity.
         *
         * @param model the ProjectEntity to convert
         * @return a new ProjectModelDTO instance
         */
        public static ProjectModelDTO createDTO(ProjectEntity model) {
            return new ProjectModelDTO(
                    model.getId(),
                    model.getDisplayName(),
                    model.getKey(),
                    model.getProjectUrl(),
                    model.getDocuUrl(),
                    model.getDescription(),
                    model.isLabor()
            );
        }
    }

    /**
     * The {@link ProjectModelErrorDTO} is used to represent an error response for project models.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "ProjectModelErrorDTO",
            description = "Error message for Project Model"
    )
    @Serdeable
    record ProjectModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ProjectModelResponseDTO, ErrorResponse {
    }
}
