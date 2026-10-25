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
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRole;
import net.onelitefeather.vulpes.backend.domain.asset.AssetTargetType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

/**
 * Connects an asset to a database model, for example a texture to an item.
 *
 * <p>A pair of asset and target is stored at most once. The target itself is not a foreign key,
 * because it can point to items, fonts or advancements.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Entity
@Table(
        name = "asset_link",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_asset_link_target",
                columnNames = {"asset_id", "target_type", "target_id"}
        )
)
public class AssetLinkEntity extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProjectAssetEntity asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 16)
    private AssetTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private AssetLinkRole role;

    /**
     * Creates an empty entity, required by JPA. Use the setters to fill it.
     */
    public AssetLinkEntity() {
    }

    public ProjectAssetEntity getAsset() {
        return asset;
    }

    public void setAsset(ProjectAssetEntity asset) {
        this.asset = asset;
    }

    public AssetTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AssetTargetType targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public AssetLinkRole getRole() {
        return role;
    }

    public void setRole(AssetLinkRole role) {
        this.role = role;
    }
}
