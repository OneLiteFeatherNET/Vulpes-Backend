package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the timestamps a create or update returns and keeps")
@EnabledIfDockerAvailable
class UpdateTimestampIntegrationTest {

    @Inject
    EmbeddedServer server;

    private String attributePath;
    private String soundPath;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelResponseDTO.ProjectModelDTO project = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Timestamps", "timestamps-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        attributePath = "/project/" + project.id() + "/attribute";
        soundPath = "/project/" + project.id() + "/sound";
    }

    @Test
    @DisplayName("update() keeps the creation date, moves the modification date and returns both")
    void update_returnsAndKeepsTimestamps() throws InterruptedException {
        AttributeModelResponseDTO.AttributeModelDTO created = given()
                .contentType(ContentType.JSON)
                .body(new AttributeModelDTO(null, "Speed", "speed", 1.0, 10.0, null))
                .post(attributePath)
                .then().statusCode(200)
                .extract().as(AttributeModelResponseDTO.AttributeModelDTO.class);
        assertNotNull(created.creationDate());
        // Timestamps have microsecond precision; make sure the update is distinguishable.
        Thread.sleep(5);

        AttributeModelResponseDTO.AttributeModelDTO updated = given()
                .contentType(ContentType.JSON)
                .body(new AttributeModelDTO(created.id(), "Speed 2", created.key(), 2.0, 20.0, null))
                .post(attributePath + "/update")
                .then().statusCode(200)
                .extract().as(AttributeModelResponseDTO.AttributeModelDTO.class);

        assertEquals(created.creationDate(), updated.creationDate(), "the response must carry the original creation date");
        assertNotNull(updated.modificationDate(), "the response must carry the new modification date");
        assertTrue(updated.modificationDate().isAfter(created.modificationDate()));

        AttributeModelResponseDTO.AttributeModelDTO reloaded = given()
                .get(attributePath + "/")
                .then().statusCode(200)
                .extract().jsonPath().getObject("content[0]", AttributeModelResponseDTO.AttributeModelDTO.class);
        assertEquals(created.creationDate(), reloaded.creationDate(), "the update must not wipe the stored creation date");
        assertEquals(updated.modificationDate(), reloaded.modificationDate());
    }

    @Test
    @DisplayName("a sound event's create and update responses carry its timestamps")
    void soundUpdate_returnsTimestamps() throws InterruptedException {
        SoundResponseDTO.SoundModelDTO created = given()
                .contentType(ContentType.JSON)
                .body(new SoundEventDTO(null, "Footsteps", "footsteps", "block.stone.step", null, null))
                .post(soundPath)
                .then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);
        assertNotNull(created.creationDate());
        Thread.sleep(5);

        SoundResponseDTO.SoundModelDTO updated = given()
                .contentType(ContentType.JSON)
                .body(new SoundEventDTO(created.id(), "Steps", created.key(), created.keyName(), null, null))
                .post(soundPath + "/update")
                .then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);

        assertEquals(created.creationDate(), updated.creationDate());
        assertTrue(updated.modificationDate().isAfter(created.modificationDate()));
    }
}
