package net.onelitefeather.vulpes.backend.domain.blockstate;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

import java.util.List;
import java.util.UUID;

/**
 * The rules for the blockstate file of one block.
 * <p>
 * A resource pack can only replace the blockstates of existing blocks, so the key is the full key of the block,
 * e.g. {@code minecraft:pink_petals}, and not a key in the namespace of the project. A project may hold several
 * blockstates for the same block, the generator writes the rules of all of them into one {@code multipart} file.
 * </p>
 *
 * @param id      the id of the blockstate, null on create
 * @param uiName  the name shown in the user interface
 * @param key     the full key of the block
 * @param comment a comment of the user
 * @param rules   the rules in the order they are written to the file
 */
@Schema(description = "The rules for the blockstate file of one block")
@Serdeable
@Introspected
public record BlockStateDTO(
        @Schema(description = "ID of the blockstate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "The name for the ui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Full key of the block, e.g. minecraft:pink_petals", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        @Pattern(regexp = BLOCK_KEY_PATTERN, message = "must be a block key like minecraft:pink_petals",
                groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Internal description of the blockstate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String comment,
        @Schema(description = "Rules in the order they are written to the file", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        List<@Valid BlockStateRuleDTO> rules
) {

    /**
     * A namespaced block key like {@code minecraft:pink_petals}.
     */
    public static final String BLOCK_KEY_PATTERN = "^(?!.*\\.\\.)[a-z0-9_.-]+:[a-z0-9_./-]+$";

    /**
     * Replaces missing rules with none.
     */
    public BlockStateDTO {
        rules = rules == null ? List.of() : rules;
    }
}
