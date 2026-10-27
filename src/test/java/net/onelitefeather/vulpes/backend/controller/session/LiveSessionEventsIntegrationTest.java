package net.onelitefeather.vulpes.backend.controller.session;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import net.onelitefeather.vulpes.backend.domain.asset.Sha1Hex;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the event stream of an asset. The stream is read over a real HTTP connection. Every wait
 * is a bounded poll on a queue that a reader thread fills; no test sleeps.
 */
@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the asset event stream with Testcontainers")
@EnabledIfDockerAvailable
class LiveSessionEventsIntegrationTest implements TestPropertyProvider {

    private static final Faker FAKER = new Faker();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_BLOCKS = 20;
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String HOST = "designer-a";

    @Inject
    EmbeddedServer server;

    @Override
    public Map<String, String> getProperties() {
        try {
            return Map.of("micronaut.object-storage.local.assets.path",
                    Files.createTempDirectory("vulpes-asset-events-").toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = server.getURL().toString();
    }

    /**
     * Creates a project with one asset and returns the path of that asset.
     */
    private String freshAssetPath() {
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "asset-events-it-" + UUID.randomUUID(), null, null, null, false);
        UUID projectId = given()
                .contentType(ContentType.JSON)
                .body(project)
        .when()
                .post("/project")
        .then()
                .statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class).id();
        String assetId = given()
                .multiPart("file", "ruby.png", AssetFixtures.png(4, 4), "image/png")
                .multiPart("key", "vulpes:live/" + UUID.randomUUID().toString().substring(0, 8))
                .multiPart("displayName", "Live")
        .when()
                .post("/project/" + projectId + "/asset")
        .then()
                .statusCode(201)
                .extract().path("id");
        return "/project/" + projectId + "/asset/" + assetId;
    }

    private EventStream open(String assetPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(server.getURL() + assetPath + "/events"))
                .header("Accept", "text/event-stream")
                .timeout(TIMEOUT)
                .GET()
                .build();
        HttpResponse<InputStream> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, response.statusCode(), "the stream of an existing asset must be answered with 200");
        return new EventStream(response.body());
    }

    private static void startSession(String assetPath) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"token\":\"%s\",\"host\":\"%s\"}".formatted(TOKEN, HOST))
        .when()
                .put(assetPath + "/live-session")
        .then()
                .statusCode(200);
    }

    private static String dataOf(List<String> block) {
        return block.stream()
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring("data:".length()).strip())
                .findFirst()
                .orElseThrow(() -> new AssertionError("the event has no data line: " + block));
    }

    @Test
    @DisplayName("the stream opens with a comment line")
    void streamOpensWithComment() throws Exception {
        String assetPath = freshAssetPath();

        try (EventStream stream = open(assetPath)) {
            List<String> first = stream.nextBlock();

            assertTrue(first.stream().allMatch(line -> line.startsWith(":")),
                    "the first frame must be a comment only: " + first);
        }
    }

    @Test
    @DisplayName("replacing the content of an asset emits one saved event with the new ETag and revision")
    void replacementEmitsOneSavedEventWithNewEtag() throws Exception {
        String assetPath = freshAssetPath();
        byte[] replacement = AssetFixtures.png(6, 6);
        String sha1 = Sha1Hex.of(replacement);

        try (EventStream stream = open(assetPath)) {
            stream.nextBlock();
            given()
                    .multiPart("file", "ruby.png", replacement, "image/png")
            .when()
                    .post(assetPath + "/content")
            .then()
                    .statusCode(200);

            JsonPath saved = JsonPath.from(dataOf(stream.nextEvent("saved")));

            assertEquals("\"" + sha1 + "\"", saved.getString("etag"),
                    "the saved event must carry the quoted SHA-1 of the new content");
            assertEquals(sha1, saved.getString("sha1"));
            assertEquals(2, saved.getInt("revision"), "the replacement of an upload is revision 2");

            startSession(assetPath);
            List<String> next = stream.nextBlock();
            assertTrue(next.stream().anyMatch(line -> line.replace(" ", "").equals("event:session")),
                    "the frame after one save must be the session event, not a second saved event: " + next);
        }
    }

    @Test
    @DisplayName("starting a session emits a started session event")
    void startingSessionEmitsStartedEvent() throws Exception {
        String assetPath = freshAssetPath();

        try (EventStream stream = open(assetPath)) {
            stream.nextBlock();
            startSession(assetPath);

            JsonPath session = JsonPath.from(dataOf(stream.nextEvent("session")));

            assertEquals("started", session.getString("state"));
            assertEquals(TOKEN, session.getString("token"));
            assertEquals(HOST, session.getString("host"));
        }
    }

    @Test
    @DisplayName("ending a session emits an ended session event")
    void endingSessionEmitsEndedEvent() throws Exception {
        String assetPath = freshAssetPath();
        startSession(assetPath);

        try (EventStream stream = open(assetPath)) {
            stream.nextBlock();
            given().queryParam("token", TOKEN)
            .when()
                    .delete(assetPath + "/live-session")
            .then()
                    .statusCode(204);

            JsonPath session = JsonPath.from(dataOf(stream.nextEvent("session")));

            assertEquals("ended", session.getString("state"));
            assertEquals(TOKEN, session.getString("token"));
        }
    }

    @Test
    @DisplayName("the stream of an unknown asset answers 404 and starts no stream")
    void streamOfUnknownAssetAnswersNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(server.getURL()
                        + "/project/" + UUID.randomUUID() + "/asset/" + UUID.randomUUID() + "/events"))
                .header("Accept", "text/event-stream")
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<Void> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(404, response.statusCode());
    }

    /**
     * Reads an event stream line by line on a daemon thread, so every read is a bounded poll on a queue.
     */
    private static final class EventStream implements AutoCloseable {

        private static final String END = "<end-of-stream>";

        private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        private final InputStream body;

        EventStream(InputStream body) {
            this.body = body;
            Thread reader = new Thread(this::pump, "asset-events-test-reader");
            reader.setDaemon(true);
            reader.start();
        }

        private void pump() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException ignored) {
                // The test closed the stream; the end marker below ends the reads.
            } finally {
                lines.add(END);
            }
        }

        /**
         * Returns the lines of the next frame, up to its blank line.
         */
        List<String> nextBlock() throws InterruptedException {
            List<String> block = new ArrayList<>();
            while (true) {
                String line = lines.poll(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                if (line == null) {
                    fail("no frame within " + TIMEOUT + ", received so far: " + block);
                }
                if (END.equals(line)) {
                    fail("the stream ended, received so far: " + block);
                }
                if (line.isEmpty()) {
                    if (!block.isEmpty()) {
                        return block;
                    }
                    continue;
                }
                block.add(line);
            }
        }

        /**
         * Returns the next frame with the given event name, skipping other frames.
         */
        List<String> nextEvent(String name) throws InterruptedException {
            for (int i = 0; i < MAX_BLOCKS; i++) {
                List<String> block = nextBlock();
                if (block.stream().anyMatch(line -> line.replace(" ", "").equals("event:" + name))) {
                    return block;
                }
            }
            fail("no '" + name + "' event within " + MAX_BLOCKS + " frames");
            return List.of();
        }

        @Override
        public void close() throws IOException {
            body.close();
        }
    }
}
