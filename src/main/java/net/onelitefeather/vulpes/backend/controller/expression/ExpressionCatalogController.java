package net.onelitefeather.vulpes.backend.controller.expression;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.service.ExpressionService;

/**
 * The {@link ExpressionCatalogController} describes the EvalEx functions, operators and constants that expressions
 * are evaluated with. It is not project-scoped, the catalog is the same for every project.
 */
@Controller("/expression")
public class ExpressionCatalogController {

    private final ExpressionService expressionService;

    @Inject
    public ExpressionCatalogController(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    @Operation(
            summary = "Get the expression catalog",
            operationId = "getExpressionCatalog",
            description = "Lists the EvalEx functions, operators and constants available to expressions.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The catalog was retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionCatalogDTO.class)
            )
    )
    @Get("/catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionCatalogDTO> catalog() {
        return HttpResponse.ok(expressionService.catalog());
    }
}
