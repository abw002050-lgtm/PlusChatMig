# V27 Build & Test Gate

1. Open the project root in Android Studio.
2. Add the real `app/google-services.json` matching `com.chatmig.modern`.
3. Sync Gradle.
4. Build `app:assembleDebug`.
5. Install the generated APK.
6. Test: Auth, Home, Users, Profile, Chat, Rooms, moderation, Points, Merchant, Transactions, Notifications, Block/Unblock.
7. Test Android 13+ notification permission and notification-to-chat deep link.
8. Test Firebase Rules against unauthorized writes.

This environment could not execute Gradle because no Gradle/Android SDK toolchain is installed.
