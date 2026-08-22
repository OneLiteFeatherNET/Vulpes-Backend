package net.onelitefeather.vulpes.backend.controller.project;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ProjectService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing projects.
 * Provides endpoints to add, retrieve, update, and delete projects.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.7.2
 */
@Controller("/project")
public class ProjectController {

    private final ProjectService projectService;

    @Inject
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Adds a new project.
     *
     * @param model the project model to be added
     * @return HttpResponse containing the added project
     */
    @Operation(
            summary = "Add a new project",
            operationId = "addProject",
            description = "Adds a new project to the database.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The project was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "The project could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ProjectModelResponseDTO> add(@Body ProjectModelDTO model) {
        ProjectModelResponseDTO.ProjectModelDTO result = projectService.create(model);
        return HttpResponse.ok(result);
    }

    /**
     * Retrieves a project by its ID.
     *
     * @param id the ID of the project to retrieve
     * @return HttpResponse containing the project if found, or not found response
     */
    @Operation(
            summary = "Get a project by ID",
            operationId = "getProjectById",
            description = "Retrieves a project from the database by its ID.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The project was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ProjectModelResponseDTO> getById(@PathVariable UUID id) {
        Optional<ProjectEntity> model = projectService.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(ProjectModelResponseDTO.ProjectModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new ProjectModelResponseDTO.ProjectModelErrorDTO("Project not found"));
    }

    /**
     * Retrieves a project by its key.
     *
     * @param key the unique key of the project to retrieve
     * @return HttpResponse containing the project if found, or not found response
     */
    @Operation(
            summary = "Get a project by key",
            operationId = "getProjectByKey",
            description = "Retrieves a project from the database by its unique key.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The project was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelErrorDTO.class)
            )
    )
    @Get("/key/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ProjectModelResponseDTO> getByKey(@PathVariable String key) {
        Optional<ProjectEntity> model = projectService.findProjectByKey(key);
        if (model.isPresent()) {
            return HttpResponse.ok(ProjectModelResponseDTO.ProjectModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new ProjectModelResponseDTO.ProjectModelErrorDTO("Project not found"));
    }

    /**
     * Updates an existing project.
     *
     * @param model the project model to update
     * @return HttpResponse containing the updated project
     */
    @Operation(
            summary = "Update a project",
            operationId = "updateProject",
            description = "Updates an existing project in the database.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The project was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ProjectModelResponseDTO> update(@Body ProjectModelDTO model) {
        ProjectModelResponseDTO result = projectService.update(model);
        if (result instanceof ProjectModelResponseDTO.ProjectModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the ID of the project to delete
     * @return HttpResponse containing the deleted project if found, or not found response
     */
    @Operation(
            summary = "Delete a project by ID",
            operationId = "deleteProjectById",
            description = "Deletes a project from the database by its ID.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The project was successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ProjectModelResponseDTO> delete(@PathVariable UUID id) {
        ProjectModelResponseDTO result = projectService.delete(id);
        if (result instanceof ProjectModelResponseDTO.ProjectModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    /**
     * Deletes all projects.
     *
     * @return HttpResponse containing an empty list
     */
    @Operation(
            summary = "Delete all projects",
            operationId = "deleteAllProjects",
            description = "Deletes all projects from the database.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All projects were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class)
            )
    )
    @Delete("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<ProjectModelResponseDTO>> deleteAll() {
        List<ProjectModelResponseDTO> result = projectService.deleteAll();
        return HttpResponse.ok(result);
    }

    /**
     * Retrieves all projects with pagination.
     *
     * @param pageable pagination parameters
     * @return HttpResponse containing a page of projects
     */
    @Operation(
            summary = "Get all projects",
            operationId = "getAllProjects",
            description = "Retrieves all projects from the database.",
            tags = {"Project"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The projects were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ProjectModelResponseDTO.ProjectModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ProjectModelResponseDTO.ProjectModelDTO>> getAll(Pageable pageable) {
        Page<ProjectModelResponseDTO.ProjectModelDTO> list = projectService.getAll(pageable);
        return HttpResponse.ok(list);
    }
}
