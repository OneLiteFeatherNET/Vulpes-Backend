package net.onelitefeather.vulpes.backend.entity.asset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import net.onelitefeather.vulpes.api.model.IdentifiableEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * A file stored for a project: a texture, a Minecraft model or a Blockbench model.
 *
 * <p>The bytes live in Micronaut Object Storage under {@code <projectId>/<assetId>}. This entity keeps
 * the metadata, so listing and validation never need to touch the storage. Textures extracted from a
 * Blockbench model point to it through {@link #getSourceAsset()}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Entity
@Table(
        name = "project_asset",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_asset_key", columnNames = {"project_id", "key"})
)
public class ProjectAssetEntity extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectEntity project;

    /** Minecraft resource location without file extension, unique per project. */
    @Column(name = "key", nullable = false, length = 200)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private AssetKind kind;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long size;

    @Column(name = "sha1", nullable = false, length = 40)
    private String sha1;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_asset_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectAssetEntity sourceAsset;

    /**
     * Creates an empty entity, required by JPA. Use the setters to fill it.
     */
    public ProjectAssetEntity() {
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AssetKind getKind() {
        return kind;
    }

    public void setKind(AssetKind kind) {
        this.kind = kind;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public ProjectAssetEntity getSourceAsset() {
        return sourceAsset;
    }

    public void setSourceAsset(ProjectAssetEntity sourceAsset) {
        this.sourceAsset = sourceAsset;
    }
}
