# V20 Chat parity

Implemented from the verified APK evidence carried forward from V19:
- private chat message model with message_id/message/message_name/message_type/message_time/message_device_time/senderId/receiverId/status
- realtime message listener
- read-status transition to `read` for received messages
- block/unblock at `block/{currentUid}/{peerUid}` and send guard
- image picker + Firebase Storage upload
- voice recording with Android RECORD_AUDIO permission + Firebase Storage upload
- bundled emoji picker using the original APK emoji asset set; emoji messages store `assetName`
- FCM token persistence and notification tap deep-link into HomeActivity/chatPeer
- notification channel for Android 8+

Not claimed as 1:1 yet: exact server-side FCM payload format, original storage paths, exact chatroom schema, audio codec/path used by the original backend, and visual pixel parity.
