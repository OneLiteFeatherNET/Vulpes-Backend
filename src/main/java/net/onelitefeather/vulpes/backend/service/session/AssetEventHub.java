package net.onelitefeather.vulpes.backend.service.session;

import io.micronaut.transaction.annotation.TransactionalEventListener;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.asset.AssetSavedEventDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fans the events of assets out to the open event streams of those assets.
 *
 * <p>Each subscriber has a unicast sink with an unbounded buffer, as the snapshot stream has. A slow client
 * therefore never loses an event; only its own buffer grows. Events are not replayed: a subscriber sees the
 * events published after it subscribed. The fan-out lives in memory of one backend instance.
 */
@Singleton
public class AssetEventHub {

    private final Set<Subscriber> subscribers = ConcurrentHashMap.newKeySet();

    /**
     * Subscribes to the events of one asset. The subscription is registered when the returned flux is subscribed,
     * and removed when it terminates.
     *
     * @param assetId the asset
     * @return the events of the asset published from now on
     */
    public Flux<AssetEvent> subscribe(UUID assetId) {
        return Flux.defer(() -> {
            Subscriber subscriber = new Subscriber(assetId);
            subscribers.add(subscriber);
            return subscriber.sink.asFlux().doFinally(signal -> subscribers.remove(subscriber));
        });
    }

    /**
     * Sends an event to the subscribers of its asset. Without subscribers the event is dropped.
     *
     * @param event the event
     */
    public void publish(AssetEvent event) {
        synchronized (this) {
            for (Subscriber subscriber : subscribers) {
                if (subscriber.assetId.equals(event.assetId())) {
                    subscriber.sink.tryEmitNext(event);
                }
            }
        }
    }

    /**
     * Announces a committed save of an asset with its new ETag and revision number.
     *
     * @param event the saved content
     */
    @TransactionalEventListener(TransactionalEventListener.TransactionPhase.AFTER_COMMIT)
    public void onAssetContentSaved(AssetContentSaved event) {
        String etag = "\"" + event.sha1() + "\"";
        publish(new AssetEvent.Saved(event.assetId(), new AssetSavedEventDTO(etag, event.sha1(), event.revision())));
    }

    /**
     * One open event stream of one asset.
     */
    private static final class Subscriber {

        private final UUID assetId;
        private final Sinks.Many<AssetEvent> sink = Sinks.many().unicast().onBackpressureBuffer();

        private Subscriber(UUID assetId) {
            this.assetId = assetId;
        }
    }
}
