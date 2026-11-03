package net.onelitefeather.vulpes.backend.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackResponse;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing the {@code pack.mcmeta} content of a project.
 *
 * <p>A project holds at most one pack meta per {@link PackType}, so a pack meta is addressed by its type
 * rather than by an identifier.
 */
public interface PackMetaService {

    /**
     * Gets every pack meta of a project, at most one per {@link PackType}.
     *
     * @param projectId the ID of the project
     * @return the pack metas of the project
     * @throws ApiException {@code PROJECT_NOT_FOUND} if the project does not exist
     */
    List<PackResponse.PackMetaDTO> getAll(@NotNull UUID projectId);

    /**
     * Gets the pack meta of a project for one pack type.
     *
     * @param projectId the ID of the project
     * @param packType  the type of pack
     * @return the pack meta
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the project has no pack meta of that type
     */
    PackResponse.PackMetaDTO get(@NotNull UUID projectId, @NotNull PackType packType);

    /**
     * Creates the pack meta of a project for one pack type, or replaces the existing one.
     *
     * @param projectId the ID of the project
     * @param packType  the type of pack
     * @param dto       the new content
     * @return the stored pack meta
     * @throws ApiException {@code PROJECT_NOT_FOUND} if the project does not exist,
     *                      {@code INVALID_REQUEST} if the format range is inverted or the description
     *                      is no JSON text component
     */
    PackResponse.PackMetaDTO put(@NotNull UUID projectId, @NotNull PackType packType, @NotNull @Valid PackMetaDTO dto);

    /**
     * Deletes the pack meta of a project for one pack type.
     *
     * @param projectId the ID of the project
     * @param packType  the type of pack
     * @return the deleted pack meta
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the project has no pack meta of that type
     */
    PackResponse.PackMetaDTO delete(@NotNull UUID projectId, @NotNull PackType packType);
}
