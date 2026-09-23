package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.font.FontRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Copies a {@link FontEntity} within the same project or into another one, optionally
 * including its characters.
 *
 * <p>Qualified {@code @Named("font")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("font")
public class FontModelCopier extends AbstractRelationalModelCopier<FontEntity, FontRelation> implements EntityCopier<FontEntity, FontRelation> {

    private final FontStringRepository fontStringRepository;

    @Inject
    public FontModelCopier(
            FontRepository fontRepository,
            FontStringRepository fontStringRepository,
            ProjectRepository projectRepository
    ) {
        super(fontRepository, projectRepository, fontRepository::existsByProjectIdAndKey, "Font");
        this.fontStringRepository = fontStringRepository;
    }

    /**
     * Widens {@link AbstractRelationalModelCopier#copy} to {@code public} and wraps it in a real
     * transaction boundary. See the class Javadoc on {@link AbstractRelationalModelCopier} for
     * why {@code @Transactional} has to be declared here, on the concrete class, rather than on
     * the inherited method itself.
     */
    @Override
    @Transactional
    public FontEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            @Nullable String targetName,
            Set<FontRelation> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, relations);
    }

    @Override
    protected FontEntity copyRoot(FontEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        FontEntity copy = new FontEntity(
                null,
                resolvedName,
                targetKey,
                source.getProvider(),
                source.getTexturePath(),
                source.getComment(),
                source.getHeight(),
                source.getAscent(),
                List.of(),
                targetProject
        );
        // mapper defaults to "font" via field initializer and is not a constructor parameter,
        // so it must be copied explicitly or a font whose mapper was changed from the default
        // would silently lose that on copy.
        copy.setMapper(source.getMapper());
        return copy;
    }

    @Override
    protected void copyRelation(FontRelation relation, FontEntity source, FontEntity target) {
        switch (relation) {
            case CHARS -> copyChars(source, target);
        }
    }

    private void copyChars(FontEntity source, FontEntity target) {
        List<FontStringEntity> copies = new ArrayList<>();
        for (FontStringEntity fontChar : fontStringRepository.findCharsByFontId(source.getId(), Pageable.unpaged()).getContent()) {
            FontStringEntity copy = new FontStringEntity(null, fontChar.getLine(), fontChar.getOrderIndex());
            copy.setFont(target);
            copies.add(copy);
        }
        fontStringRepository.saveAll(copies);
    }
}
