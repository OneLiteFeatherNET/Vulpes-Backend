# Design

## Context

- The content endpoint answers with `ETag: "<sha1>"` of the stored bytes (`AssetController.getContent`). The same value is the `sha1` field of the asset response.
- Replacement runs in `AssetPersistence.replaceContent` inside one transaction and writes the bytes to object storage in that transaction.
- Errors are raised as `ApiException` with an `ErrorCode`; `ApiExceptionHandler` turns them into RFC 9457 bodies. `ProblemDetail` has no member for extra data.
- Micronaut Data 4.14 has no lock annotation; the repository layer supports native queries.

## Decisions

**D1. Opt-in precondition.** The controller reads `If-Match` as an optional header and passes it down. Absent means no check. `*` matches any existing asset. A value matches only if it is the exact strong ETag of the current content, so weak and malformed values fail.

**D2. Compare under a row lock.** `AssetPersistence.replaceContent` first takes a `SELECT ... FOR UPDATE` on the asset row, then loads and owner-checks the asset as before, then compares the precondition, then writes. The lock lasts until commit, so two requests with the same `If-Match` serialize and the second one sees the new ETag and is refused. The lock is taken before the precondition check on purpose; checking first would allow the race.

**D3. Refusal carries the current ETag in a header.** `ProblemDetail` stays unchanged. `ApiException` gets an optional current ETag that `ApiExceptionHandler` writes as the `ETag` response header of the `412`. Body and header together let the client decide without a second request.

**D4. New error code in the existing style.** `ErrorCode.ASSET_PRECONDITION_FAILED` maps to `412`, with a short title like the other constants.

**D5. ETag on writes.** Upload (`201`) and replace (`200`) responses set `ETag` from the stored SHA-1. The `ETag` header is already exposed by CORS.

## Risks

- A client that sends `If-Match` from an old copy gets `412` and must refetch; this is the intended behaviour.
- The lock serializes replacements of the same asset only; uploads of other assets are unaffected.
