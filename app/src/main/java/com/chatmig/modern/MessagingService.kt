package com.chatmig.modern

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService:FirebaseMessagingService(){
 override fun onNewToken(token:String){com.google.firebase.auth.FirebaseAuth.getInstance().uid?.let{com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("users").child(it).child("fcmToken").setValue(token)}}
 override fun onMessageReceived(m:RemoteMessage){
  val channel="chat_messages";val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel(channel,"Chat messages",NotificationManager.IMPORTANCE_HIGH))
  val peer=m.data["chatPeer"]?:m.data["senderId"]
  val i=Intent(this,HomeActivity::class.java).apply{flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP;if(peer!=null)putExtra("chatPeer",peer)}
  val pi=PendingIntent.getActivity(this,(peer?:"chat").hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val title=m.data["title"]?:m.notification?.title?:"Chat Mig33";val body=m.data["body"]?:m.notification?.body?:"رسالة جديدة"
  val notificationId = (peer ?: System.currentTimeMillis().toString()).hashCode()
  NotificationRepo().save(AppNotification(
      type = m.data["type"] ?: "chat",
      title = title, body = body, senderId = m.data["senderId"] ?: "",
      targetId = peer ?: "", timestamp = System.currentTimeMillis(), read = false))
  nm.notify(notificationId, NotificationCompat.Builder(this,channel)
      .setSmallIcon(android.R.drawable.sym_action_chat)
      .setContentTitle(title).setContentText(body).setAutoCancel(true)
      .setContentIntent(pi).build())
 }
}
