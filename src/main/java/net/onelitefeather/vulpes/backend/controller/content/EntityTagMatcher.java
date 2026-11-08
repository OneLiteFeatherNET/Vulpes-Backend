package net.onelitefeather.vulpes.backend.controller.content;

import org.jspecify.annotations.Nullable;

/**
 * Decides whether an {@code If-None-Match} header matches the current revision of a content response.
 * A match means the client already holds the current representation and may receive 304.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class EntityTagMatcher {

    private EntityTagMatcher() {}

    /**
     * Checks an {@code If-None-Match} header against a revision. The wildcard, the strong entity tag and the
     * weak entity tag of the revision all match. The header may list several comma-separated tags.
     *
     * @param ifNoneMatch the raw header value, or {@code null} when the header is absent
     * @param revision the current revision, without quotes
     * @return {@code true} if the header matches the revision
     */
    public static boolean matches(@Nullable String ifNoneMatch, String revision) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        String strong = "\"" + revision + "\"";
        String weak = "W/" + strong;
        for (String candidate : ifNoneMatch.split(",")) {
            String trimmed = candidate.trim();
            if (trimmed.equals("*") || trimmed.equals(strong) || trimmed.equals(weak)) {
                return true;
            }
        }
        return false;
    }
}
