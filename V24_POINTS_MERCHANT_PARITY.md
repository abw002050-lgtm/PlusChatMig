# V24 — Points / Balance / Merchant

## Verified APK evidence
The original Chat Mig33 1.10 APK statically exposes the concepts/names `balance`, `merchant`/`marchant`, `mtjar`, `addmarchint`, `mirchint0`, `marchnt_balance`, `Transfer points`, `Send points`, and validation text indicating a 3000-point requirement.

## Implemented
- Realtime balance display under `balance/{uid}/points`.
- Point transfer with sender transaction and receiver transaction.
- Self-transfer and insufficient-balance validation.
- Merchant list under `merchant`.
- Merchant creation with name/description/price/owner/timestamp/active.
- Navigation from Home -> Number of points -> Merchant.

## Evidence boundary
Static APK inspection proves the feature concepts and several node/key names, but cannot prove the complete production transaction schema or server-side economic rules. The compatibility layer isolates those assumptions so they can be replaced when the original backend schema is recovered.

## Security note
Client-side checks are UX safeguards only. Production Firebase Rules/Cloud Functions should enforce balances and transfers atomically on trusted infrastructure; do not treat client writes as authoritative currency accounting.
