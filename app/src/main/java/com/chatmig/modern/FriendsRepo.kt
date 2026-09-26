package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * إدارة الأصدقاء وطلبات الصداقة
 *
 * المسارات في Firebase:
 *   /friendRequests/{toUid}/{requestId} = طلب وارد
 *   /friendRequests/{fromUid}/{requestId} = طلب صادر
 *   /friends/{uid}/{friendId}
 */
class FriendsRepo {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()
    private val me get() = auth.uid.orEmpty()

    fun friendRequests() = db.child("friendRequests")
    fun friends(uid: String = me) = db.child("friends").child(uid)

    // ═══════════ البحث عن المستخدمين ═══════════

    /** البحث بالاسم أو البريد (يستخدم users) */
    fun searchUsers(query: String, done: (List<ChatUser>) -> Unit) {
        val q = query.trim()
        if (q.isBlank()) {
            done(emptyList()); return
        }
        db.child("users").limitToFirst(500).get()
            .addOnSuccessListener { s ->
                val list = s.children.mapNotNull { it.getValue(ChatUser::class.java) }
                    .filter {
                        it.uid != me &&
                        (
                            it.name.contains(q, ignoreCase = true) ||
                            it.uid.contains(q, ignoreCase = true)
                        )
                    }
                    .take(30)
                done(list)
            }
            .addOnFailureListener { done(emptyList()) }
    }

    // ═══════════ إرسال طلب صداقة ═══════════

    fun sendFriendRequest(target: ChatUser, done: (Boolean, String?) -> Unit) {
        if (me.isBlank()) return done(false, "يجب تسجيل الدخول")
        if (target.uid == me) return done(false, "لا يمكنك إضافة نفسك")

        // 1) تأكد أنه ليس صديقًا بالفعل
        friends().child(target.uid).get().addOnSuccessListener { s ->
            if (s.exists()) {
                done(false, "هذا المستخدم صديقك بالفعل")
                return@addOnSuccessListener
            }

            // 2) تأكد من عدم وجود طلب سابق
            friendRequests().child(me).orderByChild("toId").equalTo(target.uid)
                .get().addOnSuccessListener { existing ->
                    val alreadySent = existing.children.any {
                        it.child("status").getValue(String::class.java) == "pending"
                    }
                    if (alreadySent) {
                        done(false, "لديك طلب معلّق مع هذا المستخدم")
                        return@addOnSuccessListener
                    }
                    doSend(target, done)
                }.addOnFailureListener { doSend(target, done) }
        }.addOnFailureListener { doSend(target, done) }
    }

    private fun doSend(target: ChatUser, done: (Boolean, String?) -> Unit) {
        val myUser = auth.currentUser
        val myName = myUser?.displayName ?: myUser?.email?.substringBefore("@") ?: "مستخدم"

        val outgoingKey = friendRequests().child(me).push().key ?: return done(false, "خطأ")
        val outgoing = FriendRequest(
            id = outgoingKey,
            fromId = me,
            fromName = myName,
            fromPhoto = "",
            toId = target.uid,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        val incomingKey = friendRequests().child(target.uid).push().key ?: return done(false, "خطأ")
        val incoming = FriendRequest(
            id = incomingKey,
            fromId = me,
            fromName = myName,
            fromPhoto = "",
            toId = target.uid,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        val updates = hashMapOf<String, Any?>(
            "friendRequests/$me/$outgoingKey" to outgoing,
            "friendRequests/${target.uid}/$incomingKey" to incoming
        )
        db.updateChildren(updates).addOnCompleteListener { t ->
            done(t.isSuccessful, t.exception?.localizedMessage)
        }
    }

    // ═══════════ قبول / رفض ═══════════

    /**
     * قبول طلب صداقة وارد.
     * بعد القبول، يُمنح كلا الطرفين مكافأة دعوة الصديق (+500) مرة واحدة.
     */
    fun acceptRequest(req: FriendRequest, done: (Boolean) -> Unit) {
        if (me.isBlank()) return done(false)
        if (req.fromId.isBlank()) return done(false)

        // ابحث في مساري عن الطلب الصادر المقابل (toId == req.fromId)
        friendRequests().child(me).orderByChild("toId").equalTo(req.fromId)
            .get().addOnSuccessListener { s ->
                val match = s.children.firstOrNull {
                    it.child("status").getValue(String::class.java) == "pending"
                }
                val outgoingKey = match?.key.orEmpty()

                val now = System.currentTimeMillis()
                val myUser = auth.currentUser
                val myName = myUser?.displayName ?: myUser?.email?.substringBefore("@") ?: "مستخدم"

                val updates = hashMapOf<String, Any?>(
                    // 1) اقبل الطلب الوارد عندي
                    "friendRequests/$me/${req.id}/status" to "accepted",
                    // 2) أضف الصداقة على الجانبين
                    "friends/$me/${req.fromId}" to Friendship(
                        friendId = req.fromId,
                        friendName = req.fromName.ifBlank { "مستخدم" },
                        friendPhoto = req.fromPhoto,
                        since = now
                    ),
                    "friends/${req.fromId}/$me" to Friendship(
                        friendId = me,
                        friendName = myName,
                        friendPhoto = "",
                        since = now
                    )
                )
                // 3) اقبل الطلب الصادر المقابل عند req.fromId (إن وُجد)
                if (outgoingKey.isNotBlank()) {
                    updates["friendRequests/${req.fromId}/$outgoingKey/status"] = "accepted"
                }

                db.updateChildren(updates).addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        // 🎁 مكافأة دعوة الصديق — لكلا الطرفين
                        val rewards = RewardsRepo()
                        // مكافأتي (أنا)
                        rewards.claimFriendInvite(req.fromId) { }
                        // مكافأة الطرف الآخر
                        grantFriendBonusToOther(req.fromId)
                    }
                    done(t.isSuccessful)
                }
            }
            .addOnFailureListener { done(false) }
    }

    /**
     * منح مكافأة دعوة الصديق للطرف الآخر.
     * يحاول كتابة rewards/{otherUid}/friendBonus_{me} = true
     * ثم يضيف 500 نقطة إلى balance/{otherUid}/points
     */
    private fun grantFriendBonusToOther(otherUid: String) {
        if (otherUid.isBlank()) return
        val key = "friendBonus_$me"

        db.child("rewards").child(otherUid).child(key).get()
            .addOnSuccessListener { snap ->
                if (snap.getValue(Boolean::class.java) == true) return@addOnSuccessListener
                // Add bonus
                db.child("balance").child(otherUid).child("points")
                    .runTransaction(object : Transaction.Handler {
                        override fun doTransaction(c: MutableData): Transaction.Result {
                            c.value = (c.getValue(Long::class.java) ?: 0L) + 500L
                            return Transaction.success(c)
                        }

                        override fun onComplete(e: DatabaseError?, ok: Boolean, s: DataSnapshot?) {
                            if (ok) {
                                db.child("rewards").child(otherUid).child(key).setValue(true)
                                // Log transaction for the other user
                                val txId = db.child("point_transactions").push().key ?: return
                                db.child("point_transactions").child(otherUid).child(txId)
                                    .setValue(
                                        mapOf(
                                            "transactionId" to txId,
                                            "type" to "friend_invite",
                                            "fromId" to me,
                                            "toId" to otherUid,
                                            "amount" to 500L,
                                            "note" to "دعوة صديق جديد",
                                            "createdAt" to ServerValue.TIMESTAMP,
                                            "status" to "completed"
                                        )
                                    )
                            }
                        }
                    })
            }
    }

    fun rejectRequest(req: FriendRequest, done: (Boolean) -> Unit) {
        friendRequests().child(me).child(req.id).child("status")
            .setValue("rejected").addOnCompleteListener { done(it.isSuccessful) }
    }

    fun cancelOutgoing(req: FriendRequest, done: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "friendRequests/$me/${req.id}" to null,
            "friendRequests/${req.toId}/${req.id}" to null
        )
        db.updateChildren(updates).addOnCompleteListener { done(it.isSuccessful) }
    }

    // ═══════════ إزالة صديق ═══════════

    fun removeFriend(friendId: String, done: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "friends/$me/$friendId" to null,
            "friends/$friendId/$me" to null
        )
        db.updateChildren(updates).addOnCompleteListener { done(it.isSuccessful) }
    }

    // ═══════════ المراقبة (Live) ═══════════

    fun observeFriends(onChange: (List<Friendship>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val list = s.children.mapNotNull { it.getValue(Friendship::class.java) }
                    .sortedBy { it.friendName }
                onChange(list)
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        friends().addValueEventListener(listener)
        return listener
    }

    fun removeFriendsListener(l: ValueEventListener) {
        friends().removeEventListener(l)
    }

    fun observeIncomingRequests(onChange: (List<FriendRequest>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val list = s.children
                    .mapNotNull { it.getValue(FriendRequest::class.java) }
                    .filter { it.status == "pending" && it.fromId != me }
                    .sortedByDescending { it.timestamp }
                onChange(list)
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        friendRequests().child(me).addValueEventListener(listener)
        return listener
    }

    fun removeIncomingListener(l: ValueEventListener) {
        friendRequests().child(me).removeEventListener(l)
    }

    fun observeOutgoingRequests(onChange: (List<FriendRequest>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val list = s.children
                    .mapNotNull { it.getValue(FriendRequest::class.java) }
                    .filter { it.status == "pending" && it.toId != me }
                    .sortedByDescending { it.timestamp }
                onChange(list)
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        friendRequests().child(me).addValueEventListener(listener)
        return listener
    }

    fun removeOutgoingListener(l: ValueEventListener) {
        friendRequests().child(me).removeEventListener(l)
    }
}
