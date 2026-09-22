package com.chatmig.modern

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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

    // ═══════════ الملف الشخصي ═══════════

    fun observeProfile(uid: String, onChange: (ChatUser) -> Unit): ValueEventListener {
        val l = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                s.getValue(ChatUser::class.java)?.let { onChange(it) }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        user(uid).addValueEventListener(l)
        return l
    }

    fun removeProfileListener(uid: String, l: ValueEventListener) {
        user(uid).removeEventListener(l)
    }

    fun uploadAvatar(uid: String, uri: Uri, done: (Boolean, String?) -> Unit) {
        if (uid.isBlank()) return done(false, "يجب تسجيل الدخول")
        val ref = storage.child("avatars/$uid.jpg")
        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                user(uid).child("photoUrl").setValue(url.toString())
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) done(true, url.toString())
                        else done(false, t.exception?.localizedMessage)
                    }
            }
            .addOnFailureListener { done(false, it.localizedMessage) }
    }

    fun updateProfile(
        name: String,
        bio: String,
        country: String,
        themeColor: String,
        done: (Boolean, String?) -> Unit
    ) {
        val u = auth.currentUser ?: return done(false, "يجب تسجيل الدخول")
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "bio" to bio,
            "country" to country,
            "themeColor" to themeColor,
            "uid" to u.uid
        )
        user(u.uid).updateChildren(updates).addOnCompleteListener { t ->
            if (t.isSuccessful) {
                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                u.updateProfile(req).addOnCompleteListener {
                    done(true, null)
                }
            } else {
                done(false, t.exception?.localizedMessage)
            }
        }
    }

    fun setOnline(uid: String, online: Boolean) {
        if (uid.isBlank()) return
        user(uid).child("online").setValue(online)
        user(uid).child("lastSeen").setValue(System.currentTimeMillis())
    }

    fun ensureProfile(done: (Boolean) -> Unit) {
        val u = auth.currentUser ?: return done(false)
        user(u.uid).get().addOnSuccessListener { s ->
            if (s.exists()) done(true)
            else user(u.uid).setValue(
                ChatUser(
                    uid = u.uid,
                    name = u.displayName ?: u.email?.substringBefore("@") ?: "مستخدم",
                    online = true,
                    lastSeen = System.currentTimeMillis()
                )
            ).addOnCompleteListener { done(it.isSuccessful) }
        }.addOnFailureListener { done(false) }
    }

    // ═══════════ الحجب ═══════════

    fun isBlocked(peer: String, onResult: (Boolean) -> Unit) {
        val me = auth.uid ?: return onResult(true)
        blockList(me).child(peer).get()
            .addOnSuccessListener { onResult(it.getValue(Boolean::class.java) == true) }
            .addOnFailureListener { onResult(false) }
    }

    fun setBlocked(peer: String, blocked: Boolean, done: (Boolean) -> Unit) {
        val me = auth.uid ?: return done(false)
        blockList(me).child(peer).setValue(blocked)
            .addOnCompleteListener { done(it.isSuccessful) }
    }

    // ═══════════ مؤشر الكتابة ═══════════

    fun setTyping(peer: String, typing: Boolean) {
        val me = auth.uid ?: return
        if (peer.isBlank() || peer == "private") return
        val cid = privateChatId(me, peer)
        val ref = chat(cid).child("typing").child(me)
        if (typing) ref.setValue(ServerValue.TIMESTAMP)
        else ref.removeValue()
    }

    fun observeTyping(peer: String, onChange: (Boolean) -> Unit): ValueEventListener? {
        val me = auth.uid ?: return null
        if (peer.isBlank() || peer == "private") return null
        val cid = privateChatId(me, peer)
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val ts = s.getValue(Long::class.java) ?: 0L
                val now = System.currentTimeMillis()
                onChange(ts > 0 && (now - ts) < 6000)
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        chat(cid).child("typing").child(peer).addValueEventListener(listener)
        return listener
    }

    fun removeTypingListener(peer: String, l: ValueEventListener?) {
        l ?: return
        val me = auth.uid ?: return
        if (peer.isBlank() || peer == "private") return
        val cid = privateChatId(me, peer)
        chat(cid).child("typing").child(peer).removeEventListener(l)
    }

    // ═══════════ الرسائل ═══════════

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
                    "message_name" to (me.displayName ?: me.email ?: "مستخدم"),
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
                        // إيقاف مؤشر الكتابة بعد الإرسال
                        setTyping(other, false)
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
                        x.child("senderPhoto").getValue(String::class.java).orEmpty(),
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
}
