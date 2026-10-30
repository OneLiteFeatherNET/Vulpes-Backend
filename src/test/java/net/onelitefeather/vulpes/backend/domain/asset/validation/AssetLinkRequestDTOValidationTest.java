package net.onelitefeather.vulpes.backend.domain.asset.validation;

import net.onelitefeather.vulpes.backend.domain.ValidationTestBase;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRequestDTO;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class AssetLinkRequestDTOValidationTest extends ValidationTestBase<AssetLinkRequestDTO> {

    @Test
    void nullTargetTypeIsRejected() {
        AssetLinkRequestDTO dto = new AssetLinkRequestDTO(null, UUID.randomUUID(), AssetLinkRole.TEXTURE);

        assertViolation(dto, "targetType");
    }

    @Test
    void nullTargetIdIsRejected() {
        AssetLinkRequestDTO dto = new AssetLinkRequestDTO(AssetTargetType.ITEM, null, AssetLinkRole.TEXTURE);

        assertViolation(dto, "targetId");
    }

    @Test
    void nullRoleIsRejected() {
        AssetLinkRequestDTO dto = new AssetLinkRequestDTO(AssetTargetType.ITEM, UUID.randomUUID(), null);

        assertViolation(dto, "role");
    }

    @Test
    void completeRequestHasNoViolation() {
        AssetLinkRequestDTO dto = new AssetLinkRequestDTO(AssetTargetType.FONT, UUID.randomUUID(), AssetLinkRole.MODEL);

        assertNoViolation(dto, "targetType");
    }
}
