package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;

import java.util.UUID;

/**
 * REST controller for copying notification resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/notification")
public class NotificationCopyController {

    private final EntityCopier<NotificationEntity, Void> notificationCopier;

    @Inject
    public NotificationCopyController(@Named("notification") EntityCopier<NotificationEntity, Void> notificationCopier) {
        this.notificationCopier = notificationCopier;
    }

    @Operation(
            summary = "Copy a notification",
            operationId = "copyNotification",
            description = "Copies a notification owned by the given project into the same project or another one, under a new or the same key.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notification was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The notification or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "A notification with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Body CopyDTO copyDTO
    ) {
        var copied = notificationCopier.copy(
                projectId,
                id,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO)
        );
        return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(copied));
    }
}
