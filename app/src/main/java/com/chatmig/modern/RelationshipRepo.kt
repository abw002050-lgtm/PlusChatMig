package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/** Compatibility relationship layer. Node names are isolated here so the UI can be mapped to the original backend later. */
class RelationshipRepo {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()
    private val me get() = auth.uid.orEmpty()

    fun follow(uid: String, follow: Boolean, done: (Boolean, String?) -> Unit) {
        if (me.isBlank() || uid.isBlank() || uid == me) return done(false, "عملية غير صالحة")
        val updates = hashMapOf<String, Any?>()
        updates["following/$me/$uid"] = if (follow) true else null
        updates["followers/$uid/$me"] = if (follow) true else null
        db.updateChildren(updates)
            .addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }
    }

    fun observeFollowing(uid: String, onChange: (Boolean) -> Unit) {
        db.child("following").child(me).child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                onChange(s.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    fun friends(uid: String) = db.child("friends").child(uid)
    fun comments(uid: String) = db.child("comments").child(uid)

    fun addComment(uid: String, text: String, done: (Boolean, String?) -> Unit) {
        val u = auth.currentUser ?: return done(false, "يجب تسجيل الدخول")
        val key = comments(uid).push().key ?: return done(false, "تعذر إنشاء التعليق")

        // ✅ إصلاح: استخدام System.currentTimeMillis() بدلاً من ServerValue.TIMESTAMP
        // لأن ServerValue.TIMESTAMP من نوع Map<String,String> وليس Long
        val comment = Comment(
            key,
            u.uid,
            u.displayName ?: u.email ?: "User",
            text.trim(),
            System.currentTimeMillis()
        )

        comments(uid).child(key).setValue(comment)
            .addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }
    }

    fun blockList() = db.child("block").child(me)
    fun profile(uid: String) = db.child("users").child(uid)
}
