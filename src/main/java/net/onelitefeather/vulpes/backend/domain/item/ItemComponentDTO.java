package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

import java.util.UUID;

/**
 * A data component of an item, e.g. {@code minecraft:food}.
 * <p>
 * The value is the vanilla JSON format of the component. The backend stores it as it is, the generator reads it
 * with the codec of the component.
 * </p>
 *
 * @param id           the id of the component entry, null on create
 * @param componentKey the key of the data component
 * @param value        the value in the vanilla JSON format, an empty object for components without a value
 */
@Schema(requiredProperties = {
        "componentKey",
        "value",
})
@Introspected
@Serdeable
public record ItemComponentDTO(
        @Schema(description = "ID of the component entry", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "Key of the data component, e.g. minecraft:food", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        @Pattern(regexp = KEY_PATTERN, groups = {Create.class, Update.class})
        String componentKey,
        @Schema(
                description = "Value in the vanilla JSON format, e.g. {\"nutrition\":4,\"saturation\":2.4}",
                requiredMode = Schema.RequiredMode.REQUIRED,
                implementation = Object.class
        )
        @NotNull(groups = {Create.class, Update.class})
        JsonNode value
) {

    /**
     * A namespaced key like {@code minecraft:food} or {@code minecraft:cat/variant}.
     */
    public static final String KEY_PATTERN = "^[a-z0-9_.-]+:[a-z0-9_./-]+$";
}
