# ChatMigModern V27 — GitHub Actions

## 1. رفع المشروع

فك الضغط عن `ChatMigModern_V27_GitHub.zip` ثم ارفع **محتويات المشروع** إلى مستودع GitHub جديد، بحيث يكون `build.gradle.kts` و`settings.gradle.kts` في جذر المستودع.

## 2. إعداد Firebase Secret

لأن المشروع يستخدم Firebase، يجب إضافة محتوى ملف `google-services.json` الخاص بمشروع Firebase الحقيقي كـ GitHub Actions Secret باسم:

`GOOGLE_SERVICES_JSON`

من GitHub:

`Repository → Settings → Secrets and variables → Actions → New repository secret`

Name:
`GOOGLE_SERVICES_JSON`

Secret:
الصق **محتوى ملف google-services.json كاملًا**.

لا ترفع `google-services.json` إلى GitHub.

## 3. تشغيل الاختبار من الهاتف

افتح المستودع في GitHub من المتصفح:

`Actions → Android Build - ChatMigModern V27 → Run workflow`

بعد انتهاء التشغيل:

`Actions → اختر آخر Run ناجح → Artifacts → ChatMigModern-V27-debug`

ستجد داخله:

`app-debug.apk`

## 4. معنى النتائج

- **Validate Gradle project = نجاح**: هيكل Gradle والإضافات قابلة للتحميل.
- **Build debug APK = نجاح**: المشروع تم تجميعه فعليًا.
- **Artifact موجود**: APK جاهز للتنزيل والتثبيت.
- **فشل Configure Firebase**: Secret غير موجود أو فارغ.
- **فشل Kotlin/Compose/Gradle**: نأخذ أول خطأ فعلي من الـlog ونصلحه قبل أي تطوير جديد.

## 5. الاختبار بعد تثبيت APK

اختبر بالترتيب:

1. Login/Register
2. Home
3. Users/Search
4. Profile
5. Private Chat
6. إرسال واستقبال رسالة
7. Block/Unblock
8. Rooms
9. Room Chat
10. Members/Moderation
11. Points
12. Merchant
13. Purchase/Transactions
14. Notifications
15. Emoji/Media

أي فشل في نقطة يتم إصلاحه في نسخة V27.x قبل الانتقال إلى V28.
