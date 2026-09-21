package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoomRepo {
    private val root = FirebaseDatabase.getInstance().reference.child("chatroom")
    private val auth get() = FirebaseAuth.getInstance()

    fun list() = root
    fun room(id: String) = root.child(id)
    fun members(id: String) = room(id).child("membersMap")
    fun bans(id: String) = room(id).child("ban")
    fun messages(id: String) = room(id).child("messages")

    fun create(name: String, topic: String, done: (String?, String?) -> Unit) {
        val me = auth.currentUser ?: return done(null, "يجب تسجيل الدخول")
        val id = root.push().key ?: return done(null, "تعذر إنشاء الغرفة")
        val now = ServerValue.TIMESTAMP
        val data = mapOf<String, Any>(
            "roomId" to id, "name" to name.trim(), "topic" to topic.trim(),
            "ownerId" to me.uid, "createdAt" to now, "memberCount" to 1L, "raised" to false
        )
        room(id).setValue(data).addOnSuccessListener {
            members(id).child(me.uid).setValue(mapOf("uid" to me.uid, "name" to (me.displayName ?: "User"), "role" to "owner", "joinedAt" to now, "banned" to false))
            done(id, null)
        }.addOnFailureListener { done(null, it.localizedMessage) }
    }

    fun join(id: String, done: (Boolean, String?) -> Unit) {
        val me = auth.currentUser ?: return done(false, "يجب تسجيل الدخول")
        val member = members(id).child(me.uid)
        bans(id).child(me.uid).get().addOnSuccessListener { b ->
            if (b.exists() && b.getValue(Boolean::class.java) == true) return@addOnSuccessListener done(false, "أنت محظور من هذه الغرفة")
            member.get().addOnSuccessListener { exists ->
                if (exists.exists()) return@addOnSuccessListener done(true, null)
                member.setValue(mapOf("uid" to me.uid, "name" to (me.displayName ?: "User"), "role" to "member", "joinedAt" to ServerValue.TIMESTAMP, "banned" to false)).addOnSuccessListener {
                    room(id).child("memberCount").runTransaction(object: Transaction.Handler {
                        override fun doTransaction(c: MutableData): Transaction.Result { c.value = (c.getValue(Long::class.java) ?: 0L) + 1L; return Transaction.success(c) }
                        override fun onComplete(e: DatabaseError?, committed: Boolean, s: DataSnapshot?) {}
                    }); done(true, null)
                }.addOnFailureListener { done(false, it.localizedMessage) }
            }
        }.addOnFailureListener { done(false, it.localizedMessage) }
    }

    fun leave(id: String, done: (Boolean) -> Unit) {
        val uid = auth.uid ?: return done(false)
        members(id).child(uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) room(id).child("memberCount").runTransaction(object: Transaction.Handler {
                override fun doTransaction(c: MutableData): Transaction.Result { c.value = maxOf(0L, (c.getValue(Long::class.java) ?: 1L) - 1L); return Transaction.success(c) }
                override fun onComplete(e: DatabaseError?, committed: Boolean, s: DataSnapshot?) {}
            })
            done(it.isSuccessful)
        }
    }

    fun setRole(id: String, uid: String, role: String, done: (Boolean) -> Unit) = members(id).child(uid).child("role").setValue(role).addOnCompleteListener { done(it.isSuccessful) }
    fun kick(id: String, uid: String, done: (Boolean) -> Unit) = members(id).child(uid).removeValue().addOnCompleteListener { done(it.isSuccessful) }
    fun ban(id: String, uid: String, value: Boolean, done: (Boolean) -> Unit) {
        bans(id).child(uid).setValue(value).addOnCompleteListener { ok -> if (ok.isSuccessful) members(id).child(uid).removeValue(); done(ok.isSuccessful) }
    }
    fun updateSettings(id: String, name: String, topic: String, done: (Boolean) -> Unit) = room(id).updateChildren(mapOf("name" to name.trim(), "topic" to topic.trim())).addOnCompleteListener { done(it.isSuccessful) }
    fun raise(id: String, done: (Boolean) -> Unit) = room(id).child("raised").setValue(true).addOnCompleteListener { done(it.isSuccessful) }
}
