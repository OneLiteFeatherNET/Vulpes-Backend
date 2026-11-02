package net.onelitefeather.vulpes.backend.storage;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.aws.AwsS3Operations;
import io.micronaut.objectstorage.local.LocalStorageOperations;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The implementation behind the {@code assets} store is chosen by configuration only. The context is
 * started without a database and without a server, and no bean of the S3 client makes a network call.
 */
@DisplayName("Selection of the implementation behind the asset object storage")
class AssetStorageSelectionTest {

    private static final Map<String, Object> NO_DATABASE = Map.of("datasources.default.enabled", false);

    private static final Map<String, Object> S3_ASSETS = Map.of(
            "micronaut.object-storage.local.assets.enabled", false,
            "micronaut.object-storage.aws.assets.bucket", "test-bucket",
            "aws.region", "us-east-1",
            "aws.services.s3.endpoint-override", "http://localhost:1",
            "aws.s3.path-style-access-enabled", true,
            "aws.access-key-id", "test-access-key",
            "aws.secret-key", "test-secret-key");

    @Test
    void assetStoreIsTheLocalImplementationByDefault() {
        assertAssetStore(Map.of(), LocalStorageOperations.class);
    }

    @Test
    void assetStoreIsTheS3ImplementationWhenTheLocalStoreIsDisabled() {
        assertAssetStore(S3_ASSETS, AwsS3Operations.class);
    }

    @Test
    void localAssetStoreIsSwitchedOffByTheVulpesAssetLocalEnabledVariable() {
        Map<String, Object> properties = new HashMap<>(S3_ASSETS);
        properties.remove("micronaut.object-storage.local.assets.enabled");
        properties.put("VULPES_ASSET_LOCAL_ENABLED", false);
        assertAssetStore(properties, AwsS3Operations.class);
    }

    private static void assertAssetStore(Map<String, Object> properties, Class<?> expected) {
        Map<String, Object> all = new HashMap<>(NO_DATABASE);
        all.putAll(properties);
        try (ApplicationContext context = ApplicationContext.run(all)) {
            ObjectStorageOperations<?, ?, ?> storage =
                    context.getBean(ObjectStorageOperations.class, Qualifiers.byName(AssetStorage.STORE_NAME));
            assertInstanceOf(expected, storage, "the assets store should be " + expected.getSimpleName());
        }
    }
}
