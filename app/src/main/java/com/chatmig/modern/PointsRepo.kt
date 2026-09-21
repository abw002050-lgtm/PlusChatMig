package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Points/merchant compatibility layer.
 * Exact production paths are not fully recoverable statically from the APK,
 * therefore all accounting writes are centralized here and can be migrated
 * to trusted server-side functions without changing the UI.
 */
class PointsRepo {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()
    private val uid get() = auth.uid.orEmpty()

    fun balanceRef(userId: String = uid) = db.child("balance").child(userId)
    fun merchantRef() = db.child("merchant")
    fun legacyMerchantRef() = db.child("marchant")
    fun transactionsRef(userId: String = uid) = db.child("point_transactions").child(userId)

    fun observeBalance(onChange: (Long) -> Unit, onError: (String) -> Unit = {}): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                onChange(s.child("points").getValue(Long::class.java) ?: s.getValue(Long::class.java) ?: 0L)
            }
            override fun onCancelled(e: DatabaseError) { onError(e.message) }
        }
        balanceRef().addValueEventListener(listener)
        return listener
    }
    fun removeBalanceListener(listener: ValueEventListener) = balanceRef().removeEventListener(listener)

    fun sendPoints(receiverId: String, amount: Long, done: (Boolean, String?) -> Unit) {
        val sender = uid
        if (sender.isBlank()) return done(false, "يجب تسجيل الدخول")
        if (receiverId.isBlank() || receiverId == sender) return done(false, "لا يمكنك تحويل النقاط إلى نفسك")
        if (amount <= 0L) return done(false, "أدخل عدد نقاط صحيح")
        val senderPoints = balanceRef(sender).child("points")
        senderPoints.runTransaction(object : Transaction.Handler {
            override fun doTransaction(current: MutableData): Transaction.Result {
                val p = current.getValue(Long::class.java) ?: 0L
                if (p < 3000L || p < amount) return Transaction.abort()
                current.value = p - amount
                return Transaction.success(current)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (!committed) return done(false, error?.message ?: "لا يوجد رصيد كافٍ. يجب أن تملك 3000 نقطة على الأقل")
                balanceRef(receiverId).child("points").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(c: MutableData): Transaction.Result {
                        c.value = (c.getValue(Long::class.java) ?: 0L) + amount
                        return Transaction.success(c)
                    }
                    override fun onComplete(e: DatabaseError?, ok: Boolean, s: DataSnapshot?) {
                        if (!ok) {
                            senderPoints.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(c: MutableData): Transaction.Result { c.value=(c.getValue(Long::class.java)?:0L)+amount; return Transaction.success(c) }
                                override fun onComplete(e2: DatabaseError?, c: Boolean, s2: DataSnapshot?) {}
                            })
                            return done(false, e?.message ?: "تعذر إكمال التحويل")
                        }
                        writeTransaction(PointTransaction(type="transfer", fromId=sender, toId=receiverId, amount=amount, status="completed"), done)
                    }
                })
            }
        })
    }

    fun merchantList() = merchantRef().limitToFirst(100)
    fun merchant(id: String) = merchantRef().child(id)
    fun transactions(limit: Int = 100) = transactionsRef().limitToLast(limit)

    fun addMerchant(name: String, description: String, price: Long, done: (Boolean, String?) -> Unit) {
        val owner = uid
        if (owner.isBlank()) return done(false, "يجب تسجيل الدخول")
        if (name.trim().isBlank() || price <= 0L) return done(false, "بيانات التاجر غير صحيحة")
        val id = merchantRef().push().key ?: return done(false, "تعذر إنشاء التاجر")
        merchantRef().child(id).setValue(mapOf("merchantId" to id,"name" to name.trim(),"description" to description.trim(),"price" to price,"ownerId" to owner,"createdAt" to ServerValue.TIMESTAMP,"active" to true))
            .addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }
    }

    fun setMerchantActive(id: String, active: Boolean, done: (Boolean, String?) -> Unit) =
        merchant(id).child("active").setValue(active).addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }

    fun deleteMerchant(id: String, done: (Boolean, String?) -> Unit) =
        merchant(id).removeValue().addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }

    /** Compatibility purchase flow. For real value, enforce this on trusted backend/rules. */
    fun purchaseMerchant(m: Merchant, done: (Boolean, String?) -> Unit) {
        val buyer = uid
        if (buyer.isBlank()) return done(false, "يجب تسجيل الدخول")
        if (m.merchantId.isBlank() || !m.active || m.price <= 0L) return done(false, "العرض غير متاح")
        if (buyer == m.ownerId) return done(false, "لا يمكنك شراء عرضك الخاص")
        val buyerPoints = balanceRef(buyer).child("points")
        buyerPoints.runTransaction(object : Transaction.Handler {
            override fun doTransaction(c: MutableData): Transaction.Result {
                val p = c.getValue(Long::class.java) ?: 0L
                if (p < m.price) return Transaction.abort()
                c.value = p - m.price
                return Transaction.success(c)
            }
            override fun onComplete(e: DatabaseError?, committed: Boolean, s: DataSnapshot?) {
                if (!committed) return done(false, e?.message ?: "الرصيد غير كافٍ")
                balanceRef(m.ownerId).child("points").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(c: MutableData): Transaction.Result { c.value=(c.getValue(Long::class.java)?:0L)+m.price; return Transaction.success(c) }
                    override fun onComplete(e2: DatabaseError?, ok: Boolean, s2: DataSnapshot?) {
                        if (!ok) {
                            buyerPoints.runTransaction(object : Transaction.Handler { override fun doTransaction(c: MutableData): Transaction.Result { c.value=(c.getValue(Long::class.java)?:0L)+m.price; return Transaction.success(c) }; override fun onComplete(a:DatabaseError?,b:Boolean,c:DataSnapshot?){} })
                            return done(false, e2?.message ?: "تعذر إكمال عملية الشراء")
                        }
                        writeTransaction(PointTransaction(type="merchant_purchase",fromId=buyer,toId=m.ownerId,merchantId=m.merchantId,merchantName=m.name,amount=m.price,status="completed"), done)
                    }
                })
            }
        })
    }

    private fun writeTransaction(t: PointTransaction, done: (Boolean, String?) -> Unit) {
        val id = db.child("point_transactions").push().key ?: return done(false, "تعذر إنشاء سجل العملية")
        val data = mapOf<String,Any>("transactionId" to id,"type" to t.type,"fromId" to t.fromId,"toId" to t.toId,"merchantId" to t.merchantId,"merchantName" to t.merchantName,"amount" to t.amount,"createdAt" to ServerValue.TIMESTAMP,"status" to t.status)
        val updates = hashMapOf<String,Any>("point_transactions/${t.fromId}/$id" to data)
        if (t.toId.isNotBlank()) updates["point_transactions/${t.toId}/$id"] = data
        db.updateChildren(updates).addOnCompleteListener { done(it.isSuccessful, it.exception?.localizedMessage) }
    }
}
