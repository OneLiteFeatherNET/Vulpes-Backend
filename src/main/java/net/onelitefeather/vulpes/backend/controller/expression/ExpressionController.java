package net.onelitefeather.vulpes.backend.controller.expression;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ExpressionService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

/**
 * The {@link ExpressionController} contains endpoints for managing EvalEx expressions of a project.
 */
@Controller("/project/{projectId}/expression")
public class ExpressionController {

    private static final String EXPRESSION = "Expression";
    private final ExpressionService expressionService;

    @Inject
    public ExpressionController(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    @Operation(
            summary = "Add a new expression",
            operationId = "addExpression",
            description = "Adds a new expression to the given project.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionResponseDTO.ExpressionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, or the expression does not parse. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> add(@PathVariable UUID projectId, @Valid @Body ExpressionDTO dtoModel) {
        return HttpResponse.ok(expressionService.create(projectId, dtoModel));
    }

    @Operation(
            summary = "Get an expression by its ID",
            operationId = "getExpressionById",
            description = "Retrieves an expression owned by the given project by its ID.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionResponseDTO.ExpressionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The expression was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(expressionService.findById(projectId, id)
                .map(ExpressionResponseDTO.ExpressionModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound(EXPRESSION)));
    }

    @Operation(
            summary = "Remove an expression by ID",
            operationId = "removeExpressionById",
            description = "Removes an expression owned by the given project by its ID.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionResponseDTO.ExpressionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The expression was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(expressionService.delete(projectId, id));
    }

    @Operation(
            summary = "Get all expressions",
            operationId = "getAllExpressions",
            description = "Retrieves all expressions belonging to the given project.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expressions were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ExpressionResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ExpressionResponseDTO.ExpressionModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<ExpressionResponseDTO.ExpressionModelDTO> returnValues = expressionService.getAll(projectId, pageable);
        return HttpResponse.ok(returnValues);
    }

    @Operation(
            summary = "Delete all expressions",
            operationId = "deleteAllExpressions",
            description = "Deletes all expressions belonging to the given project.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All expressions were successfully deleted from the database."
    )
    @Delete("/delete/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        expressionService.deleteAll(projectId);
        return HttpResponse.noContent();
    }

    @Operation(
            summary = "Update an expression",
            operationId = "updateExpression",
            description = "Updates an expression owned by the given project.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionResponseDTO.ExpressionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The expression was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, or the expression does not parse. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> update(@PathVariable UUID projectId, @Valid @Body ExpressionDTO model) {
        return HttpResponse.ok(expressionService.update(projectId, model));
    }

    @Operation(
            summary = "Evaluate an expression",
            operationId = "evaluateExpression",
            description = "Evaluates an expression with the given variable values. Nothing is stored.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was evaluated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionEvaluationResultDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The expression does not parse or cannot be evaluated, or a variable value does not fit its type. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionEvaluationResultDTO> evaluate(@PathVariable UUID projectId, @Valid @Body ExpressionEvaluateDTO request) {
        return HttpResponse.ok(expressionService.evaluate(request));
    }

    @Operation(
            summary = "Validate an expression",
            operationId = "validateExpression",
            description = "Checks an expression while it is edited. Nothing is stored. A parse error is reported in the body, not as a failed request.",
            tags = {"Expression"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The expression was checked. 'valid' is false and 'errors' holds the parse error when it does not parse.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionValidationResultDTO.class)
            )
    )
    @Post("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionValidationResultDTO> validate(@PathVariable UUID projectId, @Valid @Body ExpressionValidateDTO request) {
        return HttpResponse.ok(expressionService.validate(request));
    }
}
