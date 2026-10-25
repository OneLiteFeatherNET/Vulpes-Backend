package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for ExpressionController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class ExpressionControllerIntegrationTest {

    @Inject
    EmbeddedServer server;

    private static final Faker FAKER = new Faker();

    /**
     * {@link ExpressionController} is project-scoped; every request in this class routes through one
     * persisted project, created once for the whole class.
     */
    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "expression-it-" + UUID.randomUUID(), null, null, null, false);
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

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    private String expressionPath(String suffix) {
        return "/project/" + projectId + "/expression" + suffix;
    }

    private static ExpressionDTO sampleDTO(UUID id, String expression) {
        return new ExpressionDTO(
                id,
                FAKER.rockBand().name(),
                expression,
                null,
                List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, "3"),
                        new ExpressionVariableDTO("b", ExpressionVariableType.NUMBER, "1")),
                null,
                null,
                null
        );
    }

    @Test
    void testPostExpressionReturnsOkWithVariables() {
        ExpressionResponseDTO.ExpressionModelDTO created = given()
                .contentType(ContentType.JSON)
                .body(sampleDTO(null, "a * 2 + b"))
        .when()
                .post(expressionPath(""))
        .then()
                .statusCode(200)
                .body("expression", is("a * 2 + b"))
                .extract().as(ExpressionResponseDTO.ExpressionModelDTO.class);

        assertNotNull(created.id(), "the created expression must carry its id");
        assertEquals(2, created.variables().size(), "the variables must be stored with the expression");
        assertEquals("a", created.variables().getFirst().name(), "the variable order must be preserved");
    }

    @Test
    void testPostInvalidExpression_returnsBadRequestWithExpressionViolation() {
        given()
                .contentType(ContentType.JSON)
                .body(sampleDTO(null, "a * (2 + b"))
        .when()
                .post(expressionPath(""))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors[0].field", is("expression"))
                .body("errors[0].code", is("EXPRESSION_INVALID"));
    }

    @Test
    void testUpdateWithInvalidExpression_returnsBadRequestWithExpressionViolation() {
        ExpressionResponseDTO.ExpressionModelDTO created = given()
                .contentType(ContentType.JSON)
                .body(sampleDTO(null, "a * 2"))
        .when()
                .post(expressionPath(""))
        .then()
                .statusCode(200)
                .extract().as(ExpressionResponseDTO.ExpressionModelDTO.class);

        given()
                .contentType(ContentType.JSON)
                .body(sampleDTO(created.id(), "a * (2 + b"))
        .when()
                .post(expressionPath("/update"))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors[0].field", is("expression"))
                .body("errors[0].code", is("EXPRESSION_INVALID"));
    }

    @Test
    void testUpdateNotFoundReturns404() {
        given()
                .contentType(ContentType.JSON)
                .body(sampleDTO(UUID.randomUUID(), "a * 2"))
        .when()
                .post(expressionPath("/update"))
        .then()
                .statusCode(404);
    }

    @Test
    void testEvaluateReturnsValueAndType() {
        ExpressionEvaluationResultDTO result = given()
                .contentType(ContentType.JSON)
                .body(new ExpressionEvaluateDTO("a * 2 + b", Map.of("a", "3", "b", "1"), Map.of()))
        .when()
                .post(expressionPath("/evaluate"))
        .then()
                .statusCode(200)
                .extract().as(ExpressionEvaluationResultDTO.class);

        assertEquals("7", result.value(), "3 * 2 + 1 must evaluate to 7");
        assertEquals("NUMBER", result.type(), "the result must report its EvalEx data type");
    }

    @Test
    void testEvaluateWithWrongValue_returnsBadRequestWithVariableViolation() {
        given()
                .contentType(ContentType.JSON)
                .body(new ExpressionEvaluateDTO("a * 2", Map.of("a", "abc"), Map.of()))
        .when()
                .post(expressionPath("/evaluate"))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors[0].field", is("variables.a"));
    }
}
