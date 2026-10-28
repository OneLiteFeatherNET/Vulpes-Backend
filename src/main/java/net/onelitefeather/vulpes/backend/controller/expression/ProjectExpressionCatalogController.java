package net.onelitefeather.vulpes.backend.controller.expression;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.service.ExpressionService;

import java.util.UUID;

/**
 * The {@link ProjectExpressionCatalogController} describes what the expressions of one project can use: the EvalEx
 * built-ins together with the functions the project defined.
 */
@Controller("/project/{projectId}")
public class ProjectExpressionCatalogController {

    private final ExpressionService expressionService;

    @Inject
    public ProjectExpressionCatalogController(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    @Operation(
            summary = "Get the expression catalog of a project",
            operationId = "getProjectExpressionCatalog",
            description = "Lists the EvalEx functions, operators and constants, and the functions the project defined.",
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
    @Get("/expression-catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionCatalogDTO> catalog(@PathVariable UUID projectId) {
        return HttpResponse.ok(expressionService.catalog(projectId));
    }
}
