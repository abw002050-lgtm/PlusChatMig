# V23 — Relationships / Profile / Comments / Block List

Implemented:
- Profile route per user.
- Follow/unfollow with mirrored follower/following writes.
- Private chat shortcut from profile.
- Profile comments with realtime-compatible Firebase nodes.
- Block list view and unblock action.
- Kept settings/profile navigation.

Evidence boundary:
The APK proves the presence of profile, follow/friend, comments, block list and settings concepts, but its complete production Firebase schema cannot be recovered with certainty from static APK inspection alone. RelationshipRepo therefore isolates compatibility node names for later exact backend mapping.
