# V25 — Merchant purchasing and point transaction ledger

V25 deepens the merchant/points layer already introduced in V24.

## Implemented
- Merchant list with active/inactive state.
- Owner controls: enable/disable and delete.
- Buyer confirmation dialog.
- Merchant purchase flow: buyer balance decreases and merchant owner balance increases.
- Point transaction records for transfers and merchant purchases.
- Transaction history screen.
- Centralized accounting in `PointsRepo`.
- Home/points/merchant/transaction navigation.

## Security boundary
The original APK statically exposes merchant/balance concepts but does not reveal the complete production backend schema. The paths in this project are therefore compatibility paths. Client-side transactions are not sufficient for real currency accounting. A production deployment should move authorization and atomic accounting to trusted Firebase backend logic and restrictive Database Rules.
