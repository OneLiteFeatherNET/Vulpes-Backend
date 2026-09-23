package net.onelitefeather.vulpes.backend.security;

import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Reads the caller of the request being handled, for code that sits below the HTTP layer.
 *
 * <p>The service layer performs the writes that will later be stamped with {@code createdBy} and
 * {@code modifiedBy}, and it takes no {@code HttpRequest} parameter. Rather than thread one through
 * every signature, this reads the authentication off the request bound to the current thread.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@Singleton
public class CallerIdentityResolver {

    private static final String ISSUER_CLAIM = "iss";

    /**
     * Returns who is making the request currently being handled.
     *
     * <p>Empty outside a request, and empty for a request that was never authenticated. It is never
     * empty inside a secured endpoint: such a request would have been rejected before reaching it.
     *
     * @return the caller, or empty when there is none
     */
    public Optional<CallerIdentity> currentCaller() {
        return ServerRequestContext.currentRequest()
                .flatMap(request -> request.getUserPrincipal(Authentication.class))
                .map(this::toIdentity);
    }

    /**
     * Converts an authentication into the identity this backend records.
     *
     * @param authentication the authenticated caller
     * @return the identity
     */
    private CallerIdentity toIdentity(Authentication authentication) {
        Object issuer = authentication.getAttributes().get(ISSUER_CLAIM);
        return new CallerIdentity(
                issuer instanceof String value ? value : null,
                authentication.getName()
        );
    }
}
