# ChatMigModern V27 — Integration & Validation

V27 consolidates the V19–V26 feature layers into one navigation flow and raises the app version to 27.

## Integrated flows
- Auth -> Home
- Home -> Users -> Profile -> Private Chat
- Home -> Rooms -> Room Chat -> Members/Moderation
- Home -> Points -> Merchant -> Purchase -> Transactions
- Home/Profile -> Settings -> Notifications + Block List
- FCM notification payload -> chat deep link contract

## Validation performed
- ZIP/source tree extracted successfully from V26.
- AndroidManifest.xml parsed as XML.
- Gradle Kotlin DSL files present.
- Android source tree contains the expected integration classes.
- No GPS/location permissions were introduced.

## Build limitation
The execution environment does not contain a Gradle installation, Gradle wrapper distribution, or Android SDK toolchain, so a real APK compilation cannot be claimed here. The project is prepared for Android Studio/Gradle.

## Firebase requirement
A real `google-services.json` for the intended Firebase project is required before production/runtime testing. Do not use a fabricated Firebase configuration.
