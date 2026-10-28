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
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionInterfaceDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ExpressionFunctionService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

/**
 * The {@link ExpressionFunctionController} contains endpoints for managing the EvalEx functions a project defines.
 */
@Controller("/project/{projectId}/expression-function")
public class ExpressionFunctionController {

    private static final String EXPRESSION_FUNCTION = "Expression function";
    private static final String EXPRESSION_FUNCTION_TAG = "Expression function";
    private final ExpressionFunctionService functionService;

    @Inject
    public ExpressionFunctionController(ExpressionFunctionService functionService) {
        this.functionService = functionService;
    }

    @Operation(
            summary = "Add a new expression function",
            operationId = "addExpressionFunction",
            description = "Adds a function the given project can call in its expressions.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The function was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.class)
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
            description = "The request body failed validation, the name is reserved or taken, or the body does not parse. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> add(
            @PathVariable UUID projectId,
            @Valid @Body ExpressionFunctionDTO dtoModel
    ) {
        return HttpResponse.ok(functionService.create(projectId, dtoModel));
    }

    @Operation(
            summary = "Get an expression function by its ID",
            operationId = "getExpressionFunctionById",
            description = "Retrieves a function owned by the given project by its ID.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The function was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The function was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> getById(
            @PathVariable UUID projectId,
            @PathVariable UUID id
    ) {
        return HttpResponse.ok(functionService.findById(projectId, id)
                .map(ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound(EXPRESSION_FUNCTION)));
    }

    @Operation(
            summary = "Remove an expression function by ID",
            operationId = "removeExpressionFunctionById",
            description = "Removes a function owned by the given project by its ID.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The function was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The function was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> remove(
            @PathVariable UUID projectId,
            @PathVariable UUID id
    ) {
        return HttpResponse.ok(functionService.delete(projectId, id));
    }

    @Operation(
            summary = "Get all expression functions",
            operationId = "getAllExpressionFunctions",
            description = "Retrieves all functions belonging to the given project.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The functions were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ExpressionFunctionResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO>> getAll(
            @PathVariable UUID projectId,
            Pageable pageable
    ) {
        return HttpResponse.ok(functionService.getAll(projectId, pageable));
    }

    @Operation(
            summary = "Update an expression function",
            operationId = "updateExpressionFunction",
            description = "Updates a function owned by the given project.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The function was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The function was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, the name is reserved or taken, or the body does not parse. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> update(
            @PathVariable UUID projectId,
            @Valid @Body ExpressionFunctionDTO model
    ) {
        return HttpResponse.ok(functionService.update(projectId, model));
    }

    @Operation(
            summary = "Evaluate an expression function",
            operationId = "evaluateExpressionFunction",
            description = "Evaluates a function definition, which may not be saved yet, with the given argument values. The other functions of the project can be called. An INTERFACE function is evaluated with its test stub. Nothing is stored.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The function was evaluated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionEvaluationResultDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The definition is invalid, an argument does not fit its type, or the function cannot be evaluated. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionEvaluationResultDTO> evaluate(
            @PathVariable UUID projectId,
            @Valid @Body ExpressionFunctionEvaluateDTO request
    ) {
        return HttpResponse.ok(functionService.evaluate(projectId, request));
    }

    @Operation(
            summary = "Get the Java interface of the runtime functions",
            operationId = "getExpressionFunctionInterface",
            description = "Generates a Java interface with one abstract method per INTERFACE function of the project. FORMULA functions get no method.",
            tags = {EXPRESSION_FUNCTION_TAG}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The interface was generated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ExpressionFunctionInterfaceDTO.class)
            )
    )
    @Get("/interface")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ExpressionFunctionInterfaceDTO> generatedInterface(@PathVariable UUID projectId) {
        return HttpResponse.ok(functionService.interfaceSource(projectId));
    }
}
