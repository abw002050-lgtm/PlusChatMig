package com.chatmig.modern

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * MessagingService — استقبال FCM وعرض إشعار مرئي حقيقي.
 *
 * يعمل في ثلاث حالات:
 *  1. التطبيق في المقدمة    → لا نعرض إشعارًا (اختياري)
 *  2. التطبيق في الخلفية    → نعرض إشعارًا
 *  3. التطبيق مغلق          → نعرض إشعارًا (بفضل data-only payload)
 */
class MessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "chatmig_messages"
        const val CHANNEL_NAME = "رسائل الدردشة"
        const val CHANNEL_DESC = "إشعارات الرسائل الجديدة"
    }

    /**
     * يُستدعى عند استلام رسالة FCM.
     * يدعم نوعين:
     *  - notification payload (يصل تلقائيًا)
     *  - data payload (نحن نبنيه يدويًا)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val notification = message.notification

        // استخرج البيانات من أي من المصدرين
        val title = data["title"]
            ?: notification?.title
            ?: data["senderName"]
            ?: "رسالة جديدة"

        val body = data["body"]
            ?: notification?.body
            ?: data["text"]
            ?: "لديك رسالة جديدة"

        val senderId = data["senderId"] ?: data["fromId"] ?: ""
        val chatPeer = data["chatPeer"] ?: senderId

        // 1) احفظ الإشعار في Firebase (كما كان سابقًا)
        saveToInbox(title, body)

        // 2) اعرض إشعارًا مرئيًا حقيقيًا
        showNotification(title, body, chatPeer)
    }

    /** حفظ الإشعار في صندوق الإشعارات على Firebase */
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
            // لا نوقف الإشعار إذا فشل الحفظ
        }
    }

    /**
     * إنشاء وعرض الإشعار المرئي.
     * @param chatPeer معرّف الطرف الآخر لفتح المحادثة عند الضغط
     */
    private fun showNotification(title: String, body: String, chatPeer: String) {
        // أ) أنشئ قناة الإشعار (مطلوب Android 8+)
        createChannelIfNeeded()

        // ب) جهّز Intent لفتح HomeActivity على المحادثة
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

        // ج) ابنِ الإشعار
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)

        // د) أظهر الإشعار (مع فحص الإذن Android 13+)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                if (granted != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return // المستخدم لم يمنح الإذن
                }
            }
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: SecurityException) {
            // تجاهل بصمت
        }
    }

    /** إنشاء قناة الإشعارات مرة واحدة فقط (مطلوب Android 8+) */
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

    /** يُستدعى عندما يجدّد FCM التوكن — نحفظه في قاعدة البيانات */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().uid ?: return
        try {
            NotificationRepo().tokenRef().setValue(token)
        } catch (_: Exception) {
            // تجاهل بصمت
        }
    }
}
