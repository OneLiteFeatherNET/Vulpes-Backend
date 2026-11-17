package net.onelitefeather.vulpes.backend.domain.blockstate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

import java.util.List;
import java.util.Map;

/**
 * One rule of a blockstate, written as an entry of the {@code multipart} list of its file: every state matching
 * {@code when} shows {@code model}.
 *
 * @param name   a name to tell the rules apart in the editor, not part of the file
 * @param when   the allowed values per state property, a property which is missing or has no values matches any
 *               value
 * @param model  the model shown for a matching state, e.g. {@code minecraft:block/stone}
 * @param x      the rotation of the model around the x axis in steps of 90 degrees
 * @param y      the rotation of the model around the y axis in steps of 90 degrees
 * @param uvlock whether the texture keeps its orientation when the model is rotated
 */
@Schema(description = "A rule of a blockstate")
@JsonInclude(JsonInclude.Include.ALWAYS)
@Serdeable
@Introspected
public record BlockStateRuleDTO(
        @Schema(description = "Name to tell the rules apart in the editor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String name,
        @Schema(description = "Allowed values per state property, e.g. {\"facing\":[\"north\",\"east\"]}",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        Map<String, List<String>> when,
        @Schema(description = "Model shown for a matching state, e.g. minecraft:block/stone",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        @Pattern(regexp = MODEL_PATTERN, message = "must be a model id like minecraft:block/stone",
                groups = {Create.class, Update.class})
        String model,
        @Schema(description = "Rotation around the x axis: 0, 90, 180 or 270", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @QuarterTurn(groups = {Create.class, Update.class})
        int x,
        @Schema(description = "Rotation around the y axis: 0, 90, 180 or 270", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @QuarterTurn(groups = {Create.class, Update.class})
        int y,
        @Schema(description = "Whether the texture keeps its orientation when the model is rotated",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        boolean uvlock
) {

    /**
     * A model id with an optional namespace, e.g. {@code minecraft:block/stone} or {@code block/stone}. Empty for a
     * rule whose model is not chosen yet.
     */
    public static final String MODEL_PATTERN = "^(?!.*\\.\\.)(?:[a-z0-9_.-]+:)?[a-z0-9_./-]*$";

    /**
     * Replaces a missing {@code when} with an empty one, which matches every state.
     */
    public BlockStateRuleDTO {
        when = when == null ? Map.of() : when;
    }
}
