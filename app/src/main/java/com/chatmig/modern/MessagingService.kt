package com.chatmig.modern

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data

        val title = data["title"] ?: message.notification?.title ?: "إشعار جديد"
        val body = data["body"] ?: message.notification?.body ?: ""

        val note = AppNotification(
            id = "",
            title = title,
            body = body,
            read = false,
            timestamp = System.currentTimeMillis()
        )

        val repo = NotificationRepo()
        repo.save(note)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // يمكن إضافة حفظ التوكن هنا لاحقًا
    }
}
