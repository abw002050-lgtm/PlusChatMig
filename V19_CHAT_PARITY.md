# ChatMigModern V19 — Private Chat parity

This release is the private-chat implementation pass based on static evidence from the supplied Chat Mig33 1.10 APK.

## Verified APK identifiers
`praivet`, `praivet0`, `chat5as`, `chat_id`, `message`, `message_id`, `message_name`, `message_time`, `message_device_time`, `message_type`, `chat`, `chatroom`, `users`, `follow`, `block`, `notification`, `storage`, plus Firebase Auth/Realtime Database/Storage/Messaging classes and AXEmoji classes.

## Implemented in V19
- Login/register -> HomeActivity transition.
- User list/search from Realtime Database.
- Tap a user -> deterministic private conversation ID from both UIDs.
- Realtime message stream.
- Evidence-aligned message fields: message_id, message, message_name, message_type, message_time, message_device_time, senderId, receiverId, status.
- Send text message.
- Message ordering and basic status display.
- Original emoji.db and emoji image assets retained from APK extraction.

## Deliberately not guessed
Exact production Firebase rules/credentials, server-side point-transfer rules, exact media upload protocol, notification payloads, and undocumented UI micro-behavior remain evidence-gated.
