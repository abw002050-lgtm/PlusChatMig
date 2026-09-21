# V26 — Social + Notification integration

Implemented in the reconstructed project:
- Central notification inbox under `notifications/<uid>`.
- FCM messages are persisted to the inbox before the local notification is displayed.
- Notification click continues to deep-link to `HomeActivity` with `chatPeer`.
- Settings now exposes recent notifications and mark-as-read.
- Existing Follow/Unfollow, Comments and Block List remain isolated in `RelationshipRepo`.

## Evidence boundary
The original APK proves the existence of notification, follow/friend, comments and block-list functionality, but does not expose a trustworthy complete production Firebase schema. Therefore node names added here are compatibility nodes and must be mapped to the real backend before production use.
