package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.pack.PackMetaRepository;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackResponse;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.PackMetaService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link PackMetaService} interface.
 */
@Singleton
public class PackMetaServiceImpl implements PackMetaService {

    private static final String PACK_META = "Pack meta";

    private final PackMetaRepository repository;
    private final ProjectRepository projectRepository;
    private final JsonMapper jsonMapper;

    @Inject
    public PackMetaServiceImpl(PackMetaRepository repository, ProjectRepository projectRepository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public List<PackResponse.PackMetaDTO> getAll(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ApiException.projectNotFound();
        }
        return repository.findByProjectId(projectId).stream()
                .map(PackResponse.PackMetaDTO::createDTO)
                .toList();
    }

    @Override
    public PackResponse.PackMetaDTO get(UUID projectId, PackType packType) {
        return PackResponse.PackMetaDTO.createDTO(require(projectId, packType));
    }

    @Override
    public PackResponse.PackMetaDTO put(UUID projectId, PackType packType, PackMetaDTO dto) {
        if (!dto.isFormatRangeValid()) {
            throw ApiException.invalidRequest("The max format of a pack meta must not be below its min format.");
        }
        requireTextComponent(dto.description());

        Optional<PackMetaEntity> existing = repository.findByProjectIdAndPackType(projectId, packType);
        PackMetaEntity written;
        if (existing.isPresent()) {
            PackMetaEntity entity = existing.get();
            dto.applyTo(entity);
            written = repository.update(entity);
        } else {
            var project = projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
            written = repository.save(dto.toEntity(project, packType));
        }
        return PackResponse.PackMetaDTO.createDTO(reload(written));
    }

    @Override
    public PackResponse.PackMetaDTO delete(UUID projectId, PackType packType) {
        PackMetaEntity entity = require(projectId, packType);
        repository.delete(entity);
        return PackResponse.PackMetaDTO.createDTO(entity);
    }

    /**
     * Loads the pack meta of a project for one pack type.
     *
     * @param projectId the ID of the project
     * @param packType  the type of pack
     * @return the pack meta
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the project has no pack meta of that type
     */
    private PackMetaEntity require(UUID projectId, PackType packType) {
        return repository.findByProjectIdAndPackType(projectId, packType)
                .orElseThrow(() -> ApiException.notFound(PACK_META));
    }

    /**
     * Reads a just written entity back from the database, so the response carries the dates
     * Micronaut Data only fills in on the state Hibernate writes.
     *
     * @param written the entity returned by a save or update
     * @return the stored entity, or {@code written} if it can't be read back
     */
    private PackMetaEntity reload(PackMetaEntity written) {
        return repository.findById(written.getId()).orElse(written);
    }

    /**
     * Rejects a description that is no JSON value, since the column stores it as JSON.
     *
     * @param value the description
     * @throws ApiException {@code INVALID_REQUEST} if the description can't be parsed as JSON
     */
    private void requireTextComponent(String value) {
        try {
            jsonMapper.readValue(value, JsonNode.class);
        } catch (IOException exception) {
            throw ApiException.invalidRequest("The description of a pack meta must be a JSON text component.");
        }
    }
}
