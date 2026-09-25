package com.chatmig.modern

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * MessagingService - Receives FCM messages and shows rich notifications.
 *
 * Features:
 *  - Rich notification with sender name
 *  - Deep link to chat on tap
 *  - Notification channel for Android 8+
 *  - Saves notification to Firebase inbox
 *  - Handles token refresh
 */
class MessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "chatmig_messages"
        const val CHANNEL_NAME = "رسائل الدردشة"
        const val CHANNEL_DESC = "إشعارات الرسائل الجديدة"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val notification = message.notification

        // Extract from data payload (preferred) or notification payload
        val senderName = data["senderName"]
            ?: notification?.title
            ?: data["title"]
            ?: "رسالة جديدة"

        val body = data["body"]
            ?: notification?.body
            ?: data["text"]
            ?: "لديك رسالة جديدة"

        val senderId = data["senderId"] ?: data["fromId"] ?: ""
        val chatPeer = data["chatPeer"] ?: senderId

        // Save to Firebase inbox
        saveToInbox(senderName, body)

        // Show rich notification with deep link
        showNotification(senderName, body, chatPeer)
    }

    /**
     * Save notification to Firebase inbox for in-app display.
     */
    private fun saveToInbox(title: String, body: String) {
        try {
            val note = AppNotification(
                id = "",
                title = title,
                body = body,
                read = false,
                timestamp = System.currentTimeMillis()
            )
            NotificationRepo().save(note)
        } catch (_: Exception) {
            // Silent fail - do not block notification
        }
    }

    /**
     * Show a rich notification with deep link to chat.
     *
     * @param senderName Name of the sender (shown as title)
     * @param body Message text (shown as body)
     * @param chatPeer UID of the peer (used for deep link)
     */
    private fun showNotification(senderName: String, body: String, chatPeer: String) {
        createChannelIfNeeded()

        // Build deep link intent to HomeActivity
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatPeer", chatPeer)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatPeer.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Rich notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(senderName)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText("شات ميج 33")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 250, 200, 250))
            .setContentIntent(pendingIntent)
            .setColor(0xFF6750A4.toInt())
            .setShowWhen(true)

        // Show notification
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                if (granted != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: SecurityException) {
            // Permission denied - skip
        }
    }

    /**
     * Create notification channel (required for Android 8+).
     */
    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableLights(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Called when FCM token is refreshed.
     * Saves the new token to Firebase.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().uid ?: return
        try {
            NotificationRepo().tokenRef().setValue(token)
        } catch (_: Exception) {
            // Silent fail
        }
    }
}
