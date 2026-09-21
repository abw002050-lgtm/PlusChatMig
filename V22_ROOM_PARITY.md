# ChatMigModern V22 — Room administration parity

V22 extends the verified room-oriented reconstruction. Static APK evidence contains room-management language such as Create a chat room, Room settings, Kick, Cancel ban, The room is full, and You cannot control the room maker. The exact production Firebase schema is not fully recoverable from an APK, so the implementation isolates its compatibility schema in RoomRepo.

Implemented:
- create/join/leave rooms
- membersMap with owner/member/moderator roles
- member count transaction
- kick and ban
- moderator role assignment
- room settings fields name/topic
- raise room
- room message stream
- owner-only management actions

Not claimed as byte-for-byte parity: exact server rules, hidden Cloud Functions, ranking/monetization algorithms, and undocumented room fields.
