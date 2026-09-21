package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/** Central notification/inbox layer. Keep all backend node choices isolated here. */
class NotificationRepo {
    private val root = FirebaseDatabase.getInstance().reference
    private val uid get() = FirebaseAuth.getInstance().uid.orEmpty()
    fun inbox() = root.child("notifications").child(uid)
    fun save(n: AppNotification, done: (Boolean)->Unit = {}) {
        if (uid.isBlank()) return done(false)
        val key = if (n.id.isBlank()) inbox().push().key else n.id
        if (key.isNullOrBlank()) return done(false)
        val copy = n.copy(id=key)
        inbox().child(key).setValue(copy).addOnCompleteListener { done(it.isSuccessful) }
    }
    fun markRead(id:String, done:(Boolean)->Unit={}) = inbox().child(id).child("read").setValue(true).addOnCompleteListener{done(it.isSuccessful)}
    fun tokenRef() = root.child("users").child(uid).child("fcmToken")
}
