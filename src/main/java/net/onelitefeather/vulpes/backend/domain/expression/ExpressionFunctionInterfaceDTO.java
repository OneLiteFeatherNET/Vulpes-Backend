package net.onelitefeather.vulpes.backend.domain.expression;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A generated interface the game runtime implements for the INTERFACE functions of a project.
 *
 * @param language the language of the source, {@code java}
 * @param typeName the simple name of the generated type
 * @param source   the source code of the generated type
 */
@Schema(description = "A generated interface with one method per INTERFACE function")
@Serdeable
public record ExpressionFunctionInterfaceDTO(
        @Schema(description = "Language of the source, java") String language,
        @Schema(description = "Simple name of the generated type") String typeName,
        @Schema(description = "Source code of the generated type") String source
) {
}
