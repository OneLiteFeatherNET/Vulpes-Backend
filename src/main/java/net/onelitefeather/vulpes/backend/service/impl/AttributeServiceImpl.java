package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.repository.AttributeRepository;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.AttributeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the AttributeService interface.
 */
@Singleton
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;

    @Inject
    public AttributeServiceImpl(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @Override
    public AttributeModelResponseDTO.AttributeModelDTO createAttribute(AttributeModelDTO attributeModelDTO) {
        AttributeEntity attributeModel = attributeModelDTO.toAttributeModel();
        AttributeEntity savedAttributeModel = attributeRepository.save(attributeModel);
        return AttributeModelResponseDTO.AttributeModelDTO.create(savedAttributeModel);
    }

    @Override
    public AttributeModelResponseDTO updateAttribute(AttributeModelDTO attributeModelDTO) {
        Optional<AttributeEntity> modelOptional = attributeRepository.findById(attributeModelDTO.getId());
        if (modelOptional.isEmpty()) {
            return new AttributeModelResponseDTO.AttributeModelErrorDTO("Attribute not found");
        }
        AttributeEntity attributeModel = attributeModelDTO.toAttributeModel();
        attributeModel = attributeRepository.update(attributeModel);
        return AttributeModelResponseDTO.AttributeModelDTO.create(attributeModel);
    }

    @Override
    public AttributeModelResponseDTO deleteAttribute(UUID id) {
        Optional<AttributeEntity> attributeModel = attributeRepository.findById(id);
        if (attributeModel.isPresent()) {
            attributeRepository.deleteById(id);
            return AttributeModelResponseDTO.AttributeModelDTO.create(attributeModel.get());
        }
        return new AttributeModelResponseDTO.AttributeModelErrorDTO("Attribute not found");
    }

    @Override
    public List<AttributeModelResponseDTO> deleteAllAttributes() {
        attributeRepository.deleteAll();
        return List.of();
    }

    @Override
    public Page<AttributeModelResponseDTO.AttributeModelDTO> getAllAttributes(Pageable pageable) {
        return attributeRepository.findAll(pageable)
                .map(AttributeModelResponseDTO.AttributeModelDTO::create);
    }

    @Override
    public Optional<AttributeEntity> findAttributeById(UUID id) {
        return attributeRepository.findById(id);
    }
}