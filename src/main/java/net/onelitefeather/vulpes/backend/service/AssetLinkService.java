package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRequestDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetTargetType;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for linking project assets to items, fonts and advancements.
 */
public interface AssetLinkService {

    /**
     * Links an asset to a model of the same project.
     *
     * @param projectId the project
     * @param assetId   the asset to link
     * @param request   the target and the role of the link
     * @return the created link
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code VALIDATION_FAILED} on field
     *         {@code role} if the role does not fit the asset kind, {@code RESOURCE_NOT_FOUND} if the
     *         asset or the target is not in the project, {@code RESOURCE_CONFLICT} if the pair exists
     */
    AssetLinkDTO link(UUID projectId, UUID assetId, AssetLinkRequestDTO request);

    /**
     * Returns the assets linked to one model of a project.
     *
     * @param projectId  the project
     * @param targetType the kind of the model
     * @param targetId   the id of the model
     * @return the links, each with its asset embedded
     */
    List<AssetLinkDTO> getLinks(UUID projectId, AssetTargetType targetType, UUID targetId);

    /**
     * Removes a link. The asset and the model stay.
     *
     * @param projectId the project
     * @param linkId    the link
     */
    void unlink(UUID projectId, UUID linkId);
}
