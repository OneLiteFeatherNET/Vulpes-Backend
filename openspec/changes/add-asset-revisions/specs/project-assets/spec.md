# Spec Delta

## ADDED Requirements

### Requirement: Every content write creates a revision
The system SHALL create a revision for every content write of an asset. Creating an asset SHALL create revision 1. Each replacement of the content SHALL create the next revision number. Upload and replacement SHALL accept an optional `comment` form field of at most 500 characters, which the revision keeps. Revisions SHALL be immutable: a write SHALL NOT change the bytes or the metadata of an earlier revision.

#### Scenario: Upload creates revision 1
- **WHEN** a client uploads a new asset with a comment `first pass`
- **THEN** the asset has exactly one revision, number 1, with that comment and the SHA-1 digest of the uploaded bytes

#### Scenario: Replacement creates the next revision
- **WHEN** a client replaces the content of an asset that has revision 1
- **THEN** the asset has revision 2 with the SHA-1 digest of the new bytes, and revision 1 is unchanged

#### Scenario: Comment too long
- **WHEN** a client uploads or replaces content with a comment longer than 500 characters
- **THEN** the system responds with 400 `VALIDATION_FAILED` and a violation `ASSET_COMMENT_TOO_LONG` on field `comment`, and stores nothing

### Requirement: List the revisions of an asset
The system SHALL list the revisions of an asset at `GET /project/{projectId}/asset/{id}/revision` as a page, newest revision first. Each entry SHALL carry its id, number, SHA-1 digest, size, comment, creation date, and whether it is the current revision. The current revision is the one with the highest number.

#### Scenario: Newest first with the current marked
- **WHEN** an asset has revisions 1, 2 and 3 and a client lists them
- **THEN** the page starts with revision 3, which is marked as current, followed by revisions 2 and 1, which are not

#### Scenario: Revisions of an asset of another project
- **WHEN** a client lists the revisions of an asset under a project it does not belong to
- **THEN** the system responds with 404 `RESOURCE_NOT_FOUND`

### Requirement: Download a revision
The system SHALL return the bytes of one revision at `GET /project/{projectId}/asset/{id}/revision/{revisionId}/content` with the media type of the asset and an `ETag` of the SHA-1 digest of that revision. A matching `If-None-Match` SHALL answer `304`.

#### Scenario: Earlier revision is served with its own ETag
- **WHEN** a client downloads revision 1 of an asset whose current revision is 2
- **THEN** the response carries the bytes of revision 1 and the ETag of revision 1

#### Scenario: Revision of another asset
- **WHEN** a client requests a revision id that belongs to another asset
- **THEN** the system responds with 404 `RESOURCE_NOT_FOUND`

### Requirement: Restore an earlier revision
The system SHALL restore an earlier revision at `POST /project/{projectId}/asset/{id}/revision/{revisionId}/restore` by writing its bytes as a new revision with the next number. The new revision SHALL carry the comment `Restored revision <number>`, unless the request carries a `comment` query parameter. The response SHALL be `200 OK` with the asset and its new `ETag`.

#### Scenario: Restore makes old bytes current
- **WHEN** a client restores revision 1 of an asset whose current revision is 2
- **THEN** the system creates revision 3 with the bytes of revision 1 and the comment `Restored revision 1`, and answers `200 OK` with the new `ETag`; the content of the asset is the bytes of revision 1

#### Scenario: Restore a Blockbench model
- **WHEN** a Blockbench model with an extracted texture is restored to a revision that embeds another texture
- **THEN** the extracted textures of the asset are those of the restored revision

### Requirement: Restore honours the If-Match precondition
A restore SHALL accept the optional `If-Match` header of the content replacement. A value that does not match the current ETag of the asset SHALL be refused with `412 Precondition Failed` and error code `ASSET_PRECONDITION_FAILED`, and no revision SHALL be created. The response SHALL carry the current ETag in its `ETag` header. An absent `If-Match` SHALL restore unconditionally.

#### Scenario: Restore with a stale If-Match
- **WHEN** a client restores a revision with an `If-Match` header that differs from the current ETag
- **THEN** the system answers `412 Precondition Failed` with error code `ASSET_PRECONDITION_FAILED`, the response `ETag` header carries the current ETag, and no revision is created

### Requirement: Existing assets gain revision 1 lazily
An asset created before revisions existed has no revision rows. The system SHALL create its revision 1 on the first listing of its revisions or the first content replacement or restore, with the current bytes of the asset as that revision. The system SHALL NOT run a data migration at startup. A read of the content of such an asset SHALL serve its current bytes without creating a revision.

#### Scenario: Legacy asset listed for the first time
- **WHEN** a client lists the revisions of an asset created before revisions existed
- **THEN** the list contains one revision, number 1, with the SHA-1 digest and size of the asset, marked as current

#### Scenario: Legacy asset whose bytes are missing
- **WHEN** a client lists the revisions of a legacy asset whose stored bytes are missing
- **THEN** the list is returned, and downloading that revision answers the internal error response of the content endpoint

### Requirement: Deleting an asset deletes its revisions
Deleting an asset SHALL delete its revisions and the stored bytes of each revision. Deleting a Blockbench model SHALL also delete its extracted textures with their revisions.

#### Scenario: Delete removes the history
- **WHEN** an asset with revisions 1 and 2 is deleted
- **THEN** the asset, both revisions and their stored bytes are gone, and downloading revision 1 answers 404
