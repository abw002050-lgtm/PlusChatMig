package com.chatmig.modern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * LeaderboardRepo - Top users by points.
 *
 * Reads from /balance/{uid}/points and /users/{uid}
 * and returns a ranked list.
 */
class LeaderboardRepo {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth get() = FirebaseAuth.getInstance()
    private val me get() = auth.uid.orEmpty()

    data class LeaderEntry(
        val uid: String = "",
        val name: String = "",
        val photoUrl: String = "",
        val points: Long = 0L,
        val rank: Int = 0
    )

    /**
     * Fetch top N users by points.
     * Reads balance tree, sorts client-side, then fetches user info.
     */
    fun fetchTop(limit: Int = 10, done: (List<LeaderEntry>) -> Unit) {
        db.child("balance").limitToFirst(200).get()
            .addOnSuccessListener { balanceSnap ->
                // 1) Collect (uid -> points) for entries with > 0
                val pointsMap = mutableListOf<Pair<String, Long>>()
                for (child in balanceSnap.children) {
                    val uid = child.key ?: continue
                    val pts = child.child("points").getValue(Long::class.java) ?: 0L
                    if (pts > 0L) pointsMap.add(uid to pts)
                }

                // 2) Sort descending by points
                val sorted = pointsMap.sortedByDescending { it.second }.take(limit)

                if (sorted.isEmpty()) {
                    done(emptyList())
                    return@addOnSuccessListener
                }

                // 3) Fetch user info for each
                val result = mutableListOf<LeaderEntry>()
                var pending = sorted.size

                sorted.forEachIndexed { index, (uid, pts) ->
                    db.child("users").child(uid).get()
                        .addOnSuccessListener { userSnap ->
                            val name = userSnap.child("name")
                                .getValue(String::class.java).orEmpty()
                            val photo = userSnap.child("photoUrl")
                                .getValue(String::class.java).orEmpty()
                            result.add(
                                LeaderEntry(
                                    uid = uid,
                                    name = name.ifBlank { "مستخدم" },
                                    photoUrl = photo,
                                    points = pts,
                                    rank = index + 1
                                )
                            )
                            pending--
                            if (pending == 0) {
                                done(result.sortedBy { it.rank })
                            }
                        }
                        .addOnFailureListener {
                            pending--
                            if (pending == 0) {
                                done(result.sortedBy { it.rank })
                            }
                        }
                }
            }
            .addOnFailureListener {
                done(emptyList())
            }
    }

    /**
     * Fetch current user's rank.
     */
    fun fetchMyRank(done: (Int, Long) -> Unit) {
        val uid = me
        if (uid.isBlank()) return done(0, 0L)
        db.child("balance").limitToFirst(500).get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<Pair<String, Long>>()
                for (child in snap.children) {
                    val u = child.key ?: continue
                    val p = child.child("points").getValue(Long::class.java) ?: 0L
                    if (p > 0L) list.add(u to p)
                }
                val sorted = list.sortedByDescending { it.second }
                val idx = sorted.indexOfFirst { it.first == uid }
                if (idx >= 0) {
                    done(idx + 1, sorted[idx].second)
                } else {
                    done(0, 0L)
                }
            }
            .addOnFailureListener { done(0, 0L) }
    }
}
