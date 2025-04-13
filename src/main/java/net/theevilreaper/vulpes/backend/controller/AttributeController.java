package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.theevilreaper.vulpes.api.model.AttributeModel;
import net.theevilreaper.vulpes.api.repository.AttributeRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import net.theevilreaper.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.theevilreaper.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.theevilreaper.vulpes.backend.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller("/attribute")
public class AttributeController {

    private final AttributeRepository attributeRepository;

    @Inject
    public AttributeController(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @Operation(
            summary = "Add a new attribute",
            description = "Adds a new attribute to the database. The attribute is created with the given properties."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully added to the database.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "The attribute could not be added to the database.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Post
    public HttpResponse<AttributeModelResponseDTO> add(@Valid @Body AttributeModelDTO model) {
        AttributeModel attributeModel = model.toAttributeModel();
        AttributeModel savedAttributeModel = attributeRepository.save(attributeModel);
        return HttpResponse.ok(AttributeModelResponseDTO.AttributeModelDTO.create(savedAttributeModel));
    }

    @Operation(
            summary = "Get an attribute by ID",
            description = "Returns the attribute with the given ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully found.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The attribute was not found.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Post("/update")
    public HttpResponse<AttributeModelResponseDTO> update(@Valid @Body AttributeModelDTO model) {
        Optional<AttributeModel> modelOptional = attributeRepository.findById(model.getId());
        if (modelOptional.isEmpty()) {
            return HttpResponse.notFound(new AttributeModelResponseDTO.AttributeModelErrorDTO("Attribute not found"));
        }
        AttributeModel attributeModel = model.toAttributeModel();
        attributeModel = attributeRepository.update(attributeModel);
        return HttpResponse.ok(AttributeModelResponseDTO.AttributeModelDTO.create(attributeModel));
    }
    @Operation(
            summary = "Delete an attribute by ID",
            description = "Deletes the attribute with the given ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully deleted.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The attribute was not found.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    public HttpResponse<AttributeModelResponseDTO> delete(@PathVariable UUID id) {
        Optional<AttributeModel> attributeModel = attributeRepository.findById(id);
        if (attributeModel.isPresent()) {
            attributeRepository.deleteById(id);
            return HttpResponse.ok(AttributeModelResponseDTO.AttributeModelDTO.create(attributeModel.get()));
        }
        return HttpResponse.notFound(new AttributeModelResponseDTO.AttributeModelErrorDTO("Attribute not found"));
    }

    /**
     * Deletes all [AttributeModel] from the database.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Delete all attributes",
            description = "Deletes all attributes from the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "All attributes were successfully deleted.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @Delete("/deleteAll")
    public HttpResponse<List<AttributeModelResponseDTO>> deleteAll() {
        attributeRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    /**
     * Returns all [AttributeModel] which are currently persists in the database.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Get all attributes",
            description = "Gets all attributes from the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attributes were successfully retrieved.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @Get("/getAll")
    public HttpResponse<List<AttributeModelResponseDTO>> getAll() {
        List<AttributeModelResponseDTO> models = attributeRepository.findAll().stream().map(AttributeModelResponseDTO.AttributeModelDTO::create).collect(Collectors.toList());
        return HttpResponse.ok(models);
    }
}
