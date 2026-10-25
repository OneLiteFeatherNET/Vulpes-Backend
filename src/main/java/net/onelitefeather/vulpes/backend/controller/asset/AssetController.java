package net.onelitefeather.vulpes.backend.controller.asset;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetLinkRequestDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetTargetType;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AssetLinkService;
import net.onelitefeather.vulpes.backend.service.AssetService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * The {@link AssetController} contains endpoints for project assets: textures, Minecraft models and
 * Blockbench models, and their links to items, fonts and advancements.
 *
 * <p>Uploads are multipart requests with the fields {@code file} and {@code key}. Files are limited to
 * 8 MB. The content endpoint answers with the stored bytes and an ETag of their SHA-1 digest.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Controller("/project/{projectId}/asset")
public class AssetController {

    private static final String ETAG_QUOTE = "\"";

    private final AssetService assetService;
    private final AssetLinkService assetLinkService;

    /**
     * Constructs the controller.
     *
     * @param assetService     the service for assets
     * @param assetLinkService the service for links between assets and models
     */
    @Inject
    public AssetController(AssetService assetService, AssetLinkService assetLinkService) {
        this.assetService = assetService;
        this.assetLinkService = assetLinkService;
    }

    @Operation(
            summary = "Upload a new asset",
            operationId = "addAsset",
            description = "Uploads a .png (texture), .json (model) or .bbmodel (Blockbench model) file under the "
                    + "given key. Textures embedded in a Blockbench model are stored as assets of their own.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "201",
            description = "The asset was stored.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AssetResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The key or the file is invalid. 'errors' names the rejected fields and codes "
                    + "ASSET_KEY_INVALID, ASSET_UNSUPPORTED_TYPE or ASSET_INVALID_CONTENT.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The key, or a texture key extracted from a Blockbench model, is already in use.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AssetResponseDTO> add(
            @PathVariable UUID projectId,
            @Part("file") CompletedFileUpload file,
            @Nullable @Part("key") String key
    ) {
        AssetUploadDTO upload = new AssetUploadDTO(null, key, file.getFilename(), readBytes(file));
        return HttpResponse.created(assetService.create(projectId, upload));
    }

    @Operation(
            summary = "Get an asset by its ID",
            operationId = "getAssetById",
            description = "Retrieves an asset owned by the given project by its ID.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The asset was retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AssetResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AssetResponseDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(assetService.getAsset(projectId, id));
    }

    @Operation(
            summary = "Get all assets",
            operationId = "getAllAssets",
            description = "Retrieves the assets of the given project, optionally only one kind.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The assets were retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = AssetResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<AssetResponseDTO>> getAll(
            @PathVariable UUID projectId,
            @Nullable @QueryValue AssetKind kind,
            Pageable pageable
    ) {
        return HttpResponse.ok(assetService.getAll(projectId, kind, pageable));
    }

    @Operation(
            summary = "Get the content of an asset",
            operationId = "getAssetContent",
            description = "Returns the stored bytes with their media type. The ETag is the SHA-1 digest of the "
                    + "content. A matching If-None-Match answers 304.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The raw bytes of the asset.",
            headers = @io.swagger.v3.oas.annotations.headers.Header(
                    name = "ETag",
                    description = "SHA-1 digest of the content in quotes",
                    schema = @Schema(type = "string")
            )
    )
    @ApiResponse(
            responseCode = "304",
            description = "The client already holds the current content (If-None-Match matched)."
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}/content")
    public HttpResponse<byte[]> getContent(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Header(HttpHeaders.IF_NONE_MATCH) String ifNoneMatch
    ) {
        AssetContent content = assetService.getContent(projectId, id);
        String etag = ETAG_QUOTE + content.sha1() + ETAG_QUOTE;
        if (matchesEtag(ifNoneMatch, etag)) {
            return HttpResponse.<byte[]>notModified().header(HttpHeaders.ETAG, etag);
        }
        return HttpResponse.ok(content.content())
                .contentType(MediaType.of(content.contentType()))
                .header(HttpHeaders.ETAG, etag);
    }

    @Operation(
            summary = "Replace the content of an asset",
            operationId = "replaceAssetContent",
            description = "Replaces the bytes of an asset with a file of the same kind. For a Blockbench model the "
                    + "embedded textures are extracted again; links to old textures move to new textures with "
                    + "the same key.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The content was replaced.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AssetResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The file is invalid or of another kind. 'errors' names the field 'file'.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "A texture key extracted from the new Blockbench model is already in use.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/content")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AssetResponseDTO> replaceContent(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Part("file") CompletedFileUpload file
    ) {
        AssetUploadDTO upload = new AssetUploadDTO(id, null, file.getFilename(), readBytes(file));
        return HttpResponse.ok(assetService.replaceContent(projectId, id, upload));
    }

    @Operation(
            summary = "Remove an asset by ID",
            operationId = "removeAssetById",
            description = "Removes an asset, its links, the textures extracted from it and their stored bytes.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The asset was removed.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AssetResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AssetResponseDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(assetService.delete(projectId, id));
    }

    @Operation(
            summary = "Delete all assets",
            operationId = "deleteAllAssets",
            description = "Deletes all assets of the given project, their links and their stored bytes.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All assets were deleted."
    )
    @Delete("/delete/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        assetService.deleteAll(projectId);
        return HttpResponse.noContent();
    }

    @Operation(
            summary = "Link an asset to a model",
            operationId = "linkAsset",
            description = "Links an asset to an item, font or advancement of the given project.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "201",
            description = "The link was created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AssetLinkDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The body failed validation, or the role does not fit the asset kind. 'errors' names the field 'role' in the second case.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset or the target model was not found in the project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The asset is already linked to this model.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/link")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated
    public HttpResponse<AssetLinkDTO> link(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Valid @Body AssetLinkRequestDTO request
    ) {
        return HttpResponse.created(assetLinkService.link(projectId, id, request));
    }

    @Operation(
            summary = "Get the assets linked to a model",
            operationId = "getAssetLinks",
            description = "Lists the asset links of one item, font or advancement, each with its asset embedded.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The links were retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = AssetLinkDTO.class))
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The target model was not found in the project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/link")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<AssetLinkDTO>> getLinks(
            @PathVariable UUID projectId,
            @QueryValue AssetTargetType targetType,
            @QueryValue UUID targetId
    ) {
        return HttpResponse.ok(assetLinkService.getLinks(projectId, targetType, targetId));
    }

    @Operation(
            summary = "Remove an asset link",
            operationId = "unlinkAsset",
            description = "Removes a link. The asset and the model are kept.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "The link was removed."
    )
    @ApiResponse(
            responseCode = "404",
            description = "The link was not found in the project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/link/{linkId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> unlink(@PathVariable UUID projectId, @PathVariable UUID linkId) {
        assetLinkService.unlink(projectId, linkId);
        return HttpResponse.noContent();
    }

    /**
     * Reads an uploaded file into memory. Uploads are limited to 8 MB, so this is bounded.
     */
    private static byte[] readBytes(CompletedFileUpload file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw ApiException.invalidRequest("The uploaded file could not be read.");
        }
    }

    /**
     * Checks an If-None-Match header against the current ETag. Lists and the wildcard are honored.
     */
    private static boolean matchesEtag(@Nullable String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        for (String candidate : ifNoneMatch.split(",")) {
            String trimmed = candidate.trim();
            if (trimmed.equals("*") || trimmed.equals(etag) || trimmed.equals("W/" + etag)) {
                return true;
            }
        }
        return false;
    }
}
