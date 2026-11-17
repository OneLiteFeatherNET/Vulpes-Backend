package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateResponseDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateRuleDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the blockstate endpoints")
@EnabledIfDockerAvailable
class BlockStateControllerIntegrationTest {

    private static final BlockStateRuleDTO SEAT = new BlockStateRuleDTO(
            "Seat",
            Map.of("facing", List.of("north", "east"), "flower_amount", List.of("1")),
            "minecraft:block/seat",
            0,
            90,
            true
    );

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeEach
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Blockstates", "blockstates-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private String path(String suffix) {
        return "/project/" + projectId + "/blockstate" + suffix;
    }

    private BlockStateResponseDTO create(BlockStateDTO dto) {
        return given()
                .contentType(ContentType.JSON)
                .body(dto)
                .post(path(""))
                .then().statusCode(200)
                .extract().as(BlockStateResponseDTO.class);
    }

    private static BlockStateDTO petals(List<BlockStateRuleDTO> rules) {
        return new BlockStateDTO(null, "Petals", "minecraft:pink_petals", "A note", rules);
    }

    @Test
    @DisplayName("a created blockstate answers its rules as they were sent")
    void createKeepsRules() {
        BlockStateResponseDTO created = create(petals(List.of(SEAT)));

        assertNotNull(created.id());
        assertEquals(projectId, created.projectId());
        assertEquals("minecraft:pink_petals", created.key());
        assertEquals("A note", created.comment());
        assertEquals(List.of(SEAT), created.rules());
        assertNotNull(created.creationDate());
    }

    @Test
    @DisplayName("a blockstate is read back with its rules")
    void getReturnsStoredRules() {
        BlockStateResponseDTO created = create(petals(List.of(SEAT)));

        BlockStateResponseDTO found = given()
                .get(path("/" + created.id()))
                .then().statusCode(200)
                .extract().as(BlockStateResponseDTO.class);

        assertEquals(created.id(), found.id());
        assertEquals(List.of(SEAT), found.rules());
    }

    @Test
    @DisplayName("a blockstate without rules and without a when answers empty ones")
    void missingRulesAndWhenAreEmpty() {
        BlockStateRuleDTO anyState = new BlockStateRuleDTO(null, null, "minecraft:block/stone", 0, 0, false);

        BlockStateResponseDTO withoutRules = create(petals(null));
        BlockStateResponseDTO withoutWhen = create(petals(List.of(anyState)));

        assertEquals(List.of(), withoutRules.rules());
        assertEquals(Map.of(), withoutWhen.rules().getFirst().when());
    }

    @Test
    @DisplayName("an update replaces the rules")
    void updateReplacesRules() {
        BlockStateResponseDTO created = create(petals(List.of(SEAT)));
        BlockStateRuleDTO top = new BlockStateRuleDTO("Top", Map.of(), "minecraft:block/top", 180, 0, false);

        BlockStateResponseDTO updated = given()
                .contentType(ContentType.JSON)
                .body(new BlockStateDTO(created.id(), "Petals", "minecraft:pink_petals", null, List.of(top, SEAT)))
                .post(path("/update"))
                .then().statusCode(200)
                .extract().as(BlockStateResponseDTO.class);

        assertEquals(created.id(), updated.id());
        assertEquals(List.of(top, SEAT), updated.rules());
    }

    @Test
    @DisplayName("a project may hold several blockstates of the same block")
    void severalContextsPerBlock() {
        BlockStateResponseDTO first = create(petals(List.of(SEAT)));
        BlockStateResponseDTO second = create(petals(List.of()));

        assertNotEquals(first.id(), second.id());
        given()
                .get(path("/"))
                .then().statusCode(200)
                .body("content", hasSize(2));
    }

    @Test
    @DisplayName("a rotation which is not a quarter turn is rejected")
    void rotationOutsideQuarterTurnsIsRejected() {
        BlockStateRuleDTO tilted = new BlockStateRuleDTO(null, Map.of(), "minecraft:block/seat", 45, 0, false);

        given()
                .contentType(ContentType.JSON)
                .body(petals(List.of(tilted)))
                .post(path(""))
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("a block key without a namespace is rejected")
    void blockKeyWithoutNamespaceIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .body(new BlockStateDTO(null, "Petals", "pink_petals", null, List.of()))
                .post(path(""))
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("a create with an id is rejected, so it can't overwrite another blockstate")
    void createWithIdIsRejected() {
        BlockStateResponseDTO existing = create(petals(List.of(SEAT)));

        given()
                .contentType(ContentType.JSON)
                .body(new BlockStateDTO(existing.id(), "Other", "minecraft:pink_petals", null, List.of()))
                .post(path(""))
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("a blockstate of another project is not found")
    void otherProjectIsNotFound() {
        BlockStateResponseDTO created = create(petals(List.of()));
        UUID otherProject = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Other", "other-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();

        given()
                .get("/project/" + otherProject + "/blockstate/" + created.id())
                .then()
                .statusCode(404)
                .body("code", is("RESOURCE_NOT_FOUND"));
    }
}
