package net.onelitefeather.vulpes.backend.domain.copy;

import io.micronaut.core.annotation.Nullable;

import java.util.UUID;

/**
 * Common shape of a copy request body: which project to copy into, and which key to give the
 * copy. Implemented by {@link CopyDTO} for models without relations and by
 * {@link RelationalCopyDTO} for models that can selectively copy relations alongside their own
 * fields.
 */
public sealed interface CopyRequest permits CopyDTO, RelationalCopyDTO {

    @Nullable
    UUID targetProjectId();

    @Nullable
    String targetKey();

    /**
     * Reads {@link #targetProjectId()} off a possibly-absent request body.
     *
     * @param request the request body, or {@code null} when the client sent none
     * @return the requested target project, or {@code null} for the source's own project
     */
    @Nullable
    static UUID targetProjectId(@Nullable CopyRequest request) {
        return request != null ? request.targetProjectId() : null;
    }

    /**
     * Reads {@link #targetKey()} off a possibly-absent request body.
     *
     * @param request the request body, or {@code null} when the client sent none
     * @return the requested target key, or {@code null} to reuse the source's own key
     */
    @Nullable
    static String targetKey(@Nullable CopyRequest request) {
        return request != null ? request.targetKey() : null;
    }
}
