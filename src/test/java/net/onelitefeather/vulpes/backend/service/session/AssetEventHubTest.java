package net.onelitefeather.vulpes.backend.service.session;

import net.onelitefeather.vulpes.backend.domain.asset.AssetSavedEventDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Asset event hub")
class AssetEventHubTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final AssetEventHub hub = new AssetEventHub();

    @Test
    @DisplayName("a saved event reaches the subscriber of its asset")
    void savedEventReachesSubscriberOfItsAsset() {
        UUID asset = UUID.randomUUID();
        AssetEvent.Saved saved = new AssetEvent.Saved(asset, new AssetSavedEventDTO("\"abc\"", "abc", 2));

        StepVerifier.create(hub.subscribe(asset))
                .then(() -> hub.publish(saved))
                .expectNext(saved)
                .thenCancel()
                .verify(TIMEOUT);
    }

    @Test
    @DisplayName("events of other assets are not delivered to a subscriber")
    void eventsOfOtherAssetsAreNotDelivered() {
        UUID asset = UUID.randomUUID();
        AssetEvent.Saved ofOtherAsset = new AssetEvent.Saved(
                UUID.randomUUID(), new AssetSavedEventDTO("\"def\"", "def", 1));
        AssetEvent.Saved ofAsset = new AssetEvent.Saved(asset, new AssetSavedEventDTO("\"abc\"", "abc", 2));

        StepVerifier.create(hub.subscribe(asset))
                .then(() -> {
                    hub.publish(ofOtherAsset);
                    hub.publish(ofAsset);
                })
                .expectNext(ofAsset)
                .thenCancel()
                .verify(TIMEOUT);
    }

    @Test
    @DisplayName("a content save is announced with the quoted SHA-1 as ETag and the revision number")
    void contentSaveIsAnnouncedWithQuotedEtagAndRevision() {
        UUID asset = UUID.randomUUID();
        AssetEvent.Saved expected = new AssetEvent.Saved(asset, new AssetSavedEventDTO("\"abc\"", "abc", 3));

        StepVerifier.create(hub.subscribe(asset))
                .then(() -> hub.onAssetContentSaved(new AssetContentSaved(asset, "abc", 3)))
                .expectNext(expected)
                .thenCancel()
                .verify(TIMEOUT);
    }

    @Test
    @DisplayName("publishing without a subscriber succeeds")
    void publishingWithoutSubscriberSucceeds() {
        UUID asset = UUID.randomUUID();

        assertDoesNotThrow(() -> hub.publish(new AssetEvent.Saved(asset, new AssetSavedEventDTO("\"abc\"", "abc", 1))));
    }
}
