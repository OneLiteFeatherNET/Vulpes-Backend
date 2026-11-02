# Spec Delta

## ADDED Requirements

### Requirement: Content replacement honours an optional If-Match precondition
The system SHALL accept an optional `If-Match` header on the asset content replacement. When the header is present and does not match the current ETag of the asset, the system MUST refuse the replacement with `412 Precondition Failed` and MUST NOT change the asset or its stored bytes. When the header is absent, the system SHALL replace the content as before, without a precondition check.

#### Scenario: Matching If-Match replaces the content
- **WHEN** a client replaces the content of an asset with an `If-Match` header equal to the current ETag of the asset
- **THEN** the system answers `200 OK` and the asset carries the new content

#### Scenario: Stale If-Match is refused
- **WHEN** a client replaces the content of an asset with an `If-Match` header that differs from the current ETag
- **THEN** the system answers `412 Precondition Failed` with error code `ASSET_PRECONDITION_FAILED`, the response `ETag` header carries the current ETag, and the content and ETag of the asset are unchanged

#### Scenario: Missing If-Match keeps last-writer-wins
- **WHEN** a client replaces the content of an asset without an `If-Match` header
- **THEN** the system replaces the content and answers `200 OK`, regardless of the current ETag

#### Scenario: Wildcard If-Match matches any existing asset
- **WHEN** a client replaces the content of an existing asset with `If-Match: *`
- **THEN** the system answers `200 OK`

#### Scenario: Weak or malformed If-Match never matches
- **WHEN** a client replaces the content of an asset with a weak (`W/`) or unquoted `If-Match` value
- **THEN** the system answers `412 Precondition Failed` and changes nothing

### Requirement: Precondition check and write are atomic
The system MUST evaluate an `If-Match` precondition and write the replacement within one transaction under a lock on the asset, so that two replacements carrying the same `If-Match` value cannot both succeed.

#### Scenario: Concurrent replacements with the same If-Match
- **WHEN** two clients replace the content of the same asset at the same time with the same `If-Match` value equal to the current ETag
- **THEN** exactly one replacement answers `200 OK` and the other answers `412 Precondition Failed`

### Requirement: Write responses carry the ETag of the stored content
The system SHALL send an `ETag` header with the SHA-1 digest of the stored content, in quotes, on the responses of asset upload and of asset content replacement.

#### Scenario: Upload response carries the ETag
- **WHEN** a client uploads a new asset
- **THEN** the `201 Created` response carries an `ETag` header equal to the quoted SHA-1 digest of the uploaded bytes

#### Scenario: Replace response carries the new ETag
- **WHEN** a client replaces the content of an asset successfully
- **THEN** the `200 OK` response carries an `ETag` header equal to the quoted SHA-1 digest of the new bytes
