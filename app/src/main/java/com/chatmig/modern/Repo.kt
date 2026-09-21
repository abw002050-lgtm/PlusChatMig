package com.chatmig.modern

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class Repo {
    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()

    fun users() = db.child("users")
    fun user(uid: String) = users().child(uid)
    fun rooms() = db.child("chatroom")
    fun room(id: String) = rooms().child(id)
    fun chats() = db.child("chat")
    fun chat(id: String) = chats().child(id)
    fun messages(id: String) = chat(id).child("messages")

    fun blockList(uid: String = auth.uid.orEmpty()) = db.child("block").child(uid)
    fun privateChatId(a: String, b: String) = listOf(a, b).sorted().joinToString("_")

    fun isBlocked(peer: String, onResult: (Boolean) -> Unit) {
        val me = auth.uid ?: return onResult(true)
        blockList(me).child(peer).get()
            .addOnSuccessListener { onResult(it.getValue(Boolean::class.java) == true) }
            .addOnFailureListener { onResult(false) }
    }

    private fun write(
        other: String,
        type: String,
        text: String,
        mediaUrl: String = "",
        assetName: String = "",
        done: (Boolean, String?) -> Unit
    ) {
        val me = auth.currentUser ?: return done(false, "يجب تسجيل الدخول")
        isBlocked(other) { blocked ->
            if (blocked) {
                done(false, "لا يمكن الإرسال إلى مستخدم محظور")
            } else {
                val cid = privateChatId(me.uid, other)
                val key = messages(cid).push().key
                    ?: return@isBlocked done(false, "تعذر إنشاء الرسالة")
                val v = hashMapOf<String, Any>(
                    "message_id" to key,
                    "message" to text,
                    "message_name" to (me.displayName ?: me.email ?: ""),
                    "message_type" to type,
                    "message_time" to ServerValue.TIMESTAMP,
                    "message_device_time" to System.currentTimeMillis(),
                    "senderId" to me.uid,
                    "receiverId" to other,
                    "status" to "sent"
                )
                if (mediaUrl.isNotBlank()) v["mediaUrl"] = mediaUrl
                if (assetName.isNotBlank()) v["assetName"] = assetName
                messages(cid).child(key).setValue(v)
                    .addOnCompleteListener { t ->
                        done(t.isSuccessful, t.exception?.localizedMessage)
                    }
            }
        }
    }

    fun sendText(other: String, text: String, done: (Boolean, String?) -> Unit) {
        val c = text.trim()
        if (c.isEmpty()) return done(false, "الرسالة فارغة")
        write(other, "text", c, done = done)
    }

    fun sendEmoji(other: String, asset: String, done: (Boolean, String?) -> Unit) =
        write(other, "emoji", "", assetName = asset, done = done)

    fun uploadMedia(other: String, uri: Uri, type: String, done: (Boolean, String?) -> Unit) {
        val me = auth.uid ?: return done(false, "يجب تسجيل الدخول")
        val cid = privateChatId(me, other)
        val key = messages(cid).push().key ?: return done(false, "تعذر إنشاء الرسالة")
        isBlocked(other) { blocked ->
            if (blocked) {
                done(false, "لا يمكن الإرسال إلى مستخدم محظور")
            } else {
                val ref = storage.child("chat/$cid/$key")
                ref.putFile(uri)
                    .continueWithTask { ref.downloadUrl }
                    .addOnSuccessListener { url ->
                        write(other, type, "", url.toString(), done = done)
                    }
                    .addOnFailureListener { done(false, it.localizedMessage) }
            }
        }
    }

    fun observeMessages(
        other: String,
        onChange: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        val me = auth.uid ?: return object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) = onChange(emptyList())
            override fun onCancelled(e: DatabaseError) = onError(e.message.orEmpty())
        }
        val cid = privateChatId(me, other)
        val l = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val list = s.children.mapNotNull { x ->
                    ChatMessage(
                        x.key ?: x.child("message_id").getValue(String::class.java).orEmpty(),
                        x.child("senderId").getValue(String::class.java).orEmpty(),
                        x.child("message_name").getValue(String::class.java).orEmpty(),
                        x.child("message").getValue(String::class.java).orEmpty(),
                        x.child("message_type").getValue(String::class.java) ?: "text",
                        x.child("mediaUrl").getValue(String::class.java).orEmpty(),
                        x.child("assetName").getValue(String::class.java).orEmpty(),
                        x.child("message_time").getValue(Long::class.java) ?: 0L,
                        x.child("status").getValue(String::class.java) ?: "sent",
                        x.child("receiverId").getValue(String::class.java).orEmpty()
                    )
                }.sortedBy { it.timestamp }
                onChange(list)

                // ✅ إصلاح: me هنا String (وليس FirebaseUser)، لذا لا نستخدم me.uid
                s.children
                    .filter {
                        it.child("receiverId").getValue(String::class.java) == me &&
                        it.child("status").getValue(String::class.java) != "read"
                    }
                    .forEach { it.ref.child("status").setValue("read") }
            }
            override fun onCancelled(e: DatabaseError) = onError(e.message.orEmpty())
        }
        messages(cid).addValueEventListener(l)
        return l
    }

    fun removeListener(other: String, l: ValueEventListener) {
        auth.uid?.let { messages(privateChatId(it, other)).removeEventListener(l) }
    }

    fun setBlocked(peer: String, blocked: Boolean, done: (Boolean) -> Unit) {
        val me = auth.uid ?: return done(false)
        blockList(me).child(peer).setValue(blocked)
            .addOnCompleteListener { done(it.isSuccessful) }
    }

    fun ensureProfile(done: (Boolean) -> Unit) {
        val u = auth.currentUser ?: return done(false)
        user(u.uid).get().addOnSuccessListener { s ->
            if (s.exists()) done(true)
            else user(u.uid).setValue(
                ChatUser(u.uid, u.displayName ?: u.email ?: "User", online = true)
            ).addOnCompleteListener { done(it.isSuccessful) }
        }.addOnFailureListener { done(false) }
    }
}
