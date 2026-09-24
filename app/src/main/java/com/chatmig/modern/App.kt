package com.chatmig.modern

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * App — تهيئة Firebase وحفظ FCM token عند بدء التطبيق.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // حفظ FCM token عند بدء التطبيق (بعد أن يبدأ Firebase Auth)
        try {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (token.isNotBlank()) {
                    NotificationRepo().tokenRef().setValue(token)
                }
            }
        } catch (_: Exception) {
            // تجاهل بصمت
        }
    }
}
