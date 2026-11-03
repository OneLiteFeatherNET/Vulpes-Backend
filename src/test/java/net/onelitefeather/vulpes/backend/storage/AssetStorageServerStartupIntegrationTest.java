package net.onelitefeather.vulpes.backend.storage;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Starts the real HTTP server with the assets store on S3. The store is configured in the cluster, while the
 * base configuration must not define a local store under the same name, or the server cannot start.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfDockerAvailable
@DisplayName("Server startup with the assets store on S3")
class AssetStorageServerStartupIntegrationTest implements TestPropertyProvider {

    @Inject
    EmbeddedServer server;

    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                "micronaut.object-storage.aws.assets.bucket", "test-bucket",
                "aws.region", "us-east-1",
                "aws.services.s3.endpoint-override", "http://localhost:1",
                "aws.s3.path-style-access-enabled", "true",
                "aws.access-key-id", "test-access-key",
                "aws.secret-key", "test-secret-key");
    }

    @Test
    void serverStartsWithTheAssetStoreOnS3() {
        assertInstanceOf(AwsS3Operations.class, server.getApplicationContext()
                .getBean(ObjectStorageOperations.class, Qualifiers.byName(AssetStorage.STORE_NAME)));
    }
}
