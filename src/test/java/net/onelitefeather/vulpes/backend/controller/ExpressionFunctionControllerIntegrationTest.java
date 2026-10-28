package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionInterfaceDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@DisplayName("Integration tests for ExpressionFunctionController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class ExpressionFunctionControllerIntegrationTest {

    private static final Faker FAKER = new Faker();

    @Inject
    EmbeddedServer server;

    /**
     * Every test works on its own project, so the functions one test stores never change what another test sees.
     */
    private UUID projectId;

    @BeforeEach
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "function-it-" + UUID.randomUUID(), null, null, null, false);
        ProjectModelResponseDTO.ProjectModelDTO created =
                given()
                        .contentType(ContentType.JSON)
                        .body(project)
                .when()
                        .post("/project")
                .then()
                        .statusCode(200)
                        .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        projectId = created.id();
    }

    private String functionPath(String suffix) {
        return "/project/" + projectId + "/expression-function" + suffix;
    }

    private String expressionPath(String suffix) {
        return "/project/" + projectId + "/expression" + suffix;
    }

    private static ExpressionFunctionDTO formula(UUID id, String name, String body, String... parameters) {
        return function(id, name, ExpressionFunctionKind.FORMULA, body, parameters);
    }

    private static ExpressionFunctionDTO runtime(String name, String stub, String... parameters) {
        return function(null, name, ExpressionFunctionKind.INTERFACE, stub, parameters);
    }

    private static ExpressionFunctionDTO function(
            UUID id,
            String name,
            ExpressionFunctionKind kind,
            String body,
            String... parameters
    ) {
        List<ExpressionVariableDTO> variables = Arrays.stream(parameters)
                .map(parameter -> new ExpressionVariableDTO(parameter, ExpressionVariableType.NUMBER, null))
                .toList();
        return new ExpressionFunctionDTO(id, "UI " + name, name, kind, ExpressionVariableType.NUMBER, variables, body, null);
    }

    private ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO store(ExpressionFunctionDTO function) {
        return given()
                .contentType(ContentType.JSON)
                .body(function)
        .when()
                .post(functionPath(""))
        .then()
                .statusCode(200)
                .extract().as(ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.class);
    }

    @Test
    void testPostFunction_returnsOkWithStoredParameters() {
        ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO created =
                store(formula(null, "scale", "value * 2", "value"));

        assertNotNull(created.id(), "the created function must carry its id");
        assertEquals(projectId, created.projectId(), "the function must belong to the project of the path");
        assertEquals("value", created.parameters().getFirst().name(), "the parameters must be stored in order");
    }

    @Test
    void testPostFunctionWithBuiltInName_returnsBadRequestWithNameReserved() {
        given()
                .contentType(ContentType.JSON)
                .body(formula(null, "ROUND", "value", "value"))
        .when()
                .post(functionPath(""))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors[0].field", is("name"))
                .body("errors[0].code", is("FUNCTION_NAME_RESERVED"));
    }

    @Test
    void testPostFunctionWithTakenName_returnsBadRequestWithNameTaken() {
        store(formula(null, "scale", "value * 2", "value"));

        given()
                .contentType(ContentType.JSON)
                .body(formula(null, "Scale", "value * 3", "value"))
        .when()
                .post(functionPath(""))
        .then()
                .statusCode(400)
                .body("errors[0].field", is("name"))
                .body("errors[0].code", is("FUNCTION_NAME_TAKEN"));
    }

    @Test
    void testPostFormulaWithoutBody_returnsBadRequestWithBodyViolation() {
        given()
                .contentType(ContentType.JSON)
                .body(formula(null, "scale", null, "value"))
        .when()
                .post(functionPath(""))
        .then()
                .statusCode(400)
                .body("errors[0].field", is("body"))
                .body("errors[0].code", is("EXPRESSION_INVALID"));
    }

    @Test
    void testGetFunctionById_unknownId_returnsNotFound() {
        given()
        .when()
                .get(functionPath("/" + UUID.randomUUID()))
        .then()
                .statusCode(404);
    }

    @Test
    void testGetFunctionById_storedFunction_returnsIt() {
        ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO created = store(formula(null, "scale", "value * 2", "value"));

        given()
        .when()
                .get(functionPath("/" + created.id()))
        .then()
                .statusCode(200)
                .body("name", is("scale"));
    }

    @Test
    void testGetAllFunctions_listsStoredFunctionsOfProject() {
        store(formula(null, "scale", "value * 2", "value"));

        given()
        .when()
                .get(functionPath("/?page=0&size=20"))
        .then()
                .statusCode(200)
                .body("content.name", hasItem("scale"));
    }

    @Test
    void testUpdateFunction_changesBody() {
        ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO created = store(formula(null, "scale", "value * 2", "value"));

        given()
                .contentType(ContentType.JSON)
                .body(formula(created.id(), "scale", "value * 5", "value"))
        .when()
                .post(functionPath("/update"))
        .then()
                .statusCode(200)
                .body("body", is("value * 5"));
    }

    @Test
    void testUpdateFunctionNotFound_returnsNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body(formula(UUID.randomUUID(), "scale", "value * 5", "value"))
        .when()
                .post(functionPath("/update"))
        .then()
                .statusCode(404);
    }

    @Test
    void testDeleteFunction_removesIt() {
        ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO created = store(formula(null, "scale", "value * 2", "value"));

        given()
        .when()
                .delete(functionPath("/delete/" + created.id()))
        .then()
                .statusCode(200);

        given()
        .when()
                .get(functionPath("/" + created.id()))
        .then()
                .statusCode(404);
    }

    @Test
    void testEvaluateUnsavedFunction_returnsValue() {
        ExpressionEvaluationResultDTO result = given()
                .contentType(ContentType.JSON)
                .body(new ExpressionFunctionEvaluateDTO(formula(null, "scale", "value * 2", "value"), Map.of("value", "4")))
        .when()
                .post(functionPath("/evaluate"))
        .then()
                .statusCode(200)
                .extract().as(ExpressionEvaluationResultDTO.class);

        assertEquals("8", result.value(), "scale(4) must evaluate to 8");
        assertEquals("NUMBER", result.type(), "the result must report its EvalEx data type");
    }

    @Test
    void testEvaluateInterfaceFunctionWithoutStub_returnsBadRequest() {
        given()
                .contentType(ContentType.JSON)
                .body(new ExpressionFunctionEvaluateDTO(runtime("damage", null, "attack"), Map.of("attack", "3")))
        .when()
                .post(functionPath("/evaluate"))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors[0].message", org.hamcrest.Matchers.containsString("implemented at runtime"));
    }

    @Test
    void testInterface_listsInterfaceFunctionsOnly() {
        store(runtime("damage_bonus", null, "attack"));
        store(formula(null, "double_it", "value * 2", "value"));

        ExpressionFunctionInterfaceDTO generated = given()
        .when()
                .get(functionPath("/interface"))
        .then()
                .statusCode(200)
                .extract().as(ExpressionFunctionInterfaceDTO.class);

        assertEquals("java", generated.language(), "the interface must be Java");
        assertEquals("ProjectExpressionFunctions", generated.typeName(), "the interface must have the fixed type name");
        assertTrue(generated.source().contains("damageBonus(BigDecimal attack)"),
                "the INTERFACE function must get a method: " + generated.source());
        assertFalse(generated.source().contains("doubleIt"),
                "the FORMULA function must get no method: " + generated.source());
    }

    @Test
    void testProjectCatalog_listsProjectFunctionNextToBuiltIns() {
        store(runtime("damage_bonus", null, "attack"));

        ExpressionCatalogDTO catalog = given()
        .when()
                .get("/project/" + projectId + "/expression-catalog")
        .then()
                .statusCode(200)
                .body("functions.name", hasItem("damage_bonus"))
                .body("functions.name", hasItem("ROUND"))
                .extract().as(ExpressionCatalogDTO.class);

        assertTrue(catalog.functions().stream().anyMatch(function ->
                function.name().equals("damage_bonus") && function.kind() == ExpressionFunctionKind.INTERFACE),
                "the project catalog must describe the project function with its kind");
    }

    @Test
    void testExpressionEvaluate_callsSavedProjectFunction() {
        store(formula(null, "double_it", "value * 2", "value"));

        ExpressionEvaluationResultDTO result = given()
                .contentType(ContentType.JSON)
                .body(new ExpressionEvaluateDTO("double_it(a) + 1", Map.of("a", "5"), Map.of()))
        .when()
                .post(expressionPath("/evaluate"))
        .then()
                .statusCode(200)
                .extract().as(ExpressionEvaluationResultDTO.class);

        assertEquals("11", result.value(), "double_it(5) + 1 must evaluate to 11 with the saved function");
    }

    @Test
    void testExpressionCreate_usingSavedProjectFunction_isAccepted() {
        store(formula(null, "double_it", "value * 2", "value"));

        given()
                .contentType(ContentType.JSON)
                .body(new ExpressionDTO(null, "Damage", "double_it(a)", null,
                        List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, "3")),
                        null, null, null))
        .when()
                .post(expressionPath(""))
        .then()
                .statusCode(200)
                .body("expression", is("double_it(a)"));
    }

    @Test
    void testExpressionCreate_callingUndefinedFunction_returnsBadRequestWithExpressionViolation() {
        given()
                .contentType(ContentType.JSON)
                .body(new ExpressionDTO(null, "Damage", "missing_fn(a)", null,
                        List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, "3")),
                        null, null, null))
        .when()
                .post(expressionPath(""))
        .then()
                .statusCode(400)
                .body("errors[0].field", is("expression"))
                .body("errors[0].code", is("EXPRESSION_INVALID"));
    }
}
