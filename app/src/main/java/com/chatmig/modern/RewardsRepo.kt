package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * RewardsRepo - Daily rewards, streak bonuses, and one-time achievements.
 *
 * Firebase paths:
 *   rewards/{uid}/lastDailyClaim   - timestamp of last daily reward claim
 *   rewards/{uid}/streak           - consecutive days count
 *   rewards/{uid}/lastStreakDay    - last day (yyyyMMdd) streak was updated
 *   rewards/{uid}/welcomeClaimed   - bool: welcome bonus given
 *   rewards/{uid}/profileCompleted - bool: profile completion bonus given
 */
class RewardsRepo {

    companion object {
        const val DAILY_REWARD = 10L
        const val STREAK_BONUS = 50L
        const val WELCOME_BONUS = 50L
        const val PROFILE_BONUS = 100L
        const val STREAK_TARGET = 7
        const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    }

    private val db = FirebaseDatabase.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()
    private val uid get() = auth.uid.orEmpty()

    fun rewardsRef(userId: String = uid) = db.child("rewards").child(userId)
    fun balanceRef(userId: String = uid) = db.child("balance").child(userId).child("points")

    /**
     * Result of a claim attempt.
     */
    data class ClaimResult(
        val success: Boolean,
        val pointsEarned: Long = 0,
        val message: String = "",
        val isStreakBonus: Boolean = false,
        val newStreak: Int = 0
    )

    /**
     * Observe reward state (for UI display).
     */
    data class RewardState(
        val canClaimDaily: Boolean = false,
        val nextClaimInMs: Long = 0L,
        val currentStreak: Int = 0,
        val welcomeClaimed: Boolean = false,
        val profileClaimed: Boolean = false
    )

    fun observeState(onChange: (RewardState) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val now = System.currentTimeMillis()
                val lastDaily = s.child("lastDailyClaim").getValue(Long::class.java) ?: 0L
                val streak = s.child("streak").getValue(Int::class.java) ?: 0
                val welcome = s.child("welcomeClaimed").getValue(Boolean::class.java) ?: false
                val profile = s.child("profileCompleted").getValue(Boolean::class.java) ?: false

                val timeSince = now - lastDaily
                val canClaim = timeSince >= ONE_DAY_MS
                val nextIn = if (canClaim) 0L else (ONE_DAY_MS - timeSince)

                onChange(
                    RewardState(
                        canClaimDaily = canClaim,
                        nextClaimInMs = nextIn,
                        currentStreak = streak,
                        welcomeClaimed = welcome,
                        profileClaimed = profile
                    )
                )
            }

            override fun onCancelled(e: DatabaseError) {
                onChange(RewardState())
            }
        }
        rewardsRef().addValueEventListener(listener)
        return listener
    }

    fun removeStateListener(l: ValueEventListener) {
        rewardsRef().removeEventListener(l)
    }

    /**
     * Claim the daily reward.
     * - Adds DAILY_REWARD points.
     * - If it's a consecutive day, increments streak.
     * - If streak reaches STREAK_TARGET, awards STREAK_BONUS and resets streak.
     */
    fun claimDaily(done: (ClaimResult) -> Unit) {
        if (uid.isBlank()) return done(ClaimResult(false, message = "يجب تسجيل الدخول"))

        rewardsRef().get().addOnSuccessListener { s ->
            val now = System.currentTimeMillis()
            val lastDaily = s.child("lastDailyClaim").getValue(Long::class.java) ?: 0L
            val lastStreakDay = s.child("lastStreakDay").getValue(String::class.java) ?: ""
            val streak = s.child("streak").getValue(Int::class.java) ?: 0

            if (now - lastDaily < ONE_DAY_MS) {
                val remain = ONE_DAY_MS - (now - lastDaily)
                val hours = remain / (60 * 60 * 1000)
                return@addOnSuccessListener done(
                    ClaimResult(false, message = "عد بعد $hours ساعة")
                )
            }

            val today = todayKey()
            val yesterday = yesterdayKey()
            val newStreak = when {
                lastStreakDay == yesterday -> streak + 1
                lastStreakDay == today -> streak      // already counted today
                else -> 1                              // streak broken
            }

            val isBonus = newStreak >= STREAK_TARGET
            val pointsToAdd = DAILY_REWARD + if (isBonus) STREAK_BONUS else 0L
            val finalStreak = if (isBonus) 0 else newStreak

            // Update balance
            balanceRef().runTransaction(object : Transaction.Handler {
                override fun doTransaction(c: MutableData): Transaction.Result {
                    c.value = (c.getValue(Long::class.java) ?: 0L) + pointsToAdd
                    return Transaction.success(c)
                }

                override fun onComplete(e: DatabaseError?, ok: Boolean, snap: DataSnapshot?) {
                    if (!ok) {
                        return done(ClaimResult(false, message = e?.message ?: "فشل"))
                    }
                    // Record reward metadata
                    val updates = hashMapOf<String, Any>(
                        "lastDailyClaim" to now,
                        "streak" to finalStreak,
                        "lastStreakDay" to today
                    )
                    rewardsRef().updateChildren(updates).addOnCompleteListener {
                        // Log transaction
                        logTransaction(
                            type = "daily_reward",
                            amount = pointsToAdd,
                            note = if (isBonus) "مكافأة سلسلة 7 أيام" else "مكافأة يومية"
                        )
                        done(
                            ClaimResult(
                                success = true,
                                pointsEarned = pointsToAdd,
                                message = if (isBonus)
                                    "🎉 مبروك! مكافأة السلسلة +$STREAK_BONUS"
                                else
                                    "✅ تم استلام $pointsToAdd نقطة",
                                isStreakBonus = isBonus,
                                newStreak = finalStreak
                            )
                        )
                    }
                }
            })
        }.addOnFailureListener {
            done(ClaimResult(false, message = it.localizedMessage ?: "خطأ"))
        }
    }

    /**
     * Claim one-time welcome bonus.
     */
    fun claimWelcome(done: (ClaimResult) -> Unit) {
        if (uid.isBlank()) return done(ClaimResult(false, message = "يجب تسجيل الدخول"))

        rewardsRef().child("welcomeClaimed").get().addOnSuccessListener { s ->
            if (s.getValue(Boolean::class.java) == true) {
                return@addOnSuccessListener done(ClaimResult(false, message = "تم استلام مكافأة الترحيب"))
            }
            balanceRef().runTransaction(object : Transaction.Handler {
                override fun doTransaction(c: MutableData): Transaction.Result {
                    c.value = (c.getValue(Long::class.java) ?: 0L) + WELCOME_BONUS
                    return Transaction.success(c)
                }

                override fun onComplete(e: DatabaseError?, ok: Boolean, snap: DataSnapshot?) {
                    if (!ok) return done(ClaimResult(false, message = "فشل"))
                    rewardsRef().child("welcomeClaimed").setValue(true)
                    logTransaction("welcome_bonus", WELCOME_BONUS, "مكافأة ترحيب")
                    done(
                        ClaimResult(
                            success = true,
                            pointsEarned = WELCOME_BONUS,
                            message = "🎉 مرحبًا! +$WELCOME_BONUS نقطة"
                        )
                    )
                }
            })
        }
    }

    /**
     * Claim profile completion bonus.
     */
    fun claimProfileComplete(done: (ClaimResult) -> Unit) {
        if (uid.isBlank()) return done(ClaimResult(false, message = "يجب تسجيل الدخول"))

        rewardsRef().child("profileCompleted").get().addOnSuccessListener { s ->
            if (s.getValue(Boolean::class.java) == true) {
                return@addOnSuccessListener done(
                    ClaimResult(false, message = "تم استلام مكافأة إكمال الملف")
                )
            }
            balanceRef().runTransaction(object : Transaction.Handler {
                override fun doTransaction(c: MutableData): Transaction.Result {
                    c.value = (c.getValue(Long::class.java) ?: 0L) + PROFILE_BONUS
                    return Transaction.success(c)
                }

                override fun onComplete(e: DatabaseError?, ok: Boolean, snap: DataSnapshot?) {
                    if (!ok) return done(ClaimResult(false, message = "فشل"))
                    rewardsRef().child("profileCompleted").setValue(true)
                    logTransaction("profile_complete", PROFILE_BONUS, "إكمال الملف الشخصي")
                    done(
                        ClaimResult(
                            success = true,
                            pointsEarned = PROFILE_BONUS,
                            message = "🎉 +$PROFILE_BONUS لإكمال ملفك!"
                        )
                    )
                }
            })
        }
    }

    // ═══════════ Helpers ═══════════

    private fun todayKey(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d%02d%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun yesterdayKey(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
        return "%04d%02d%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun logTransaction(type: String, amount: Long, note: String) {
        val id = db.child("point_transactions").push().key ?: return
        val data = mapOf(
            "transactionId" to id,
            "type" to type,
            "fromId" to "system",
            "toId" to uid,
            "amount" to amount,
            "note" to note,
            "createdAt" to ServerValue.TIMESTAMP,
            "status" to "completed"
        )
        db.child("point_transactions").child(uid).child(id).setValue(data)
    }
}
