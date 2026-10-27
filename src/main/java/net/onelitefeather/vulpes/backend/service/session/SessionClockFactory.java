package net.onelitefeather.vulpes.backend.service.session;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.time.Clock;

/**
 * Provides the clock that decides when live sessions expire. Tests build the registry with their own clock.
 */
@Factory
public class SessionClockFactory {

    /**
     * Returns the system clock in UTC.
     *
     * @return the clock
     */
    @Singleton
    public Clock clock() {
        return Clock.systemUTC();
    }
}
