package com.chatmig.modern

data class Balance(val points: Long = 0L)
data class Merchant(
    val merchantId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0L,
    val ownerId: String = "",
    val createdAt: Long = 0L,
    val active: Boolean = true
)
data class PointTransaction(
    val transactionId: String = "",
    val type: String = "",
    val fromId: String = "",
    val toId: String = "",
    val merchantId: String = "",
    val merchantName: String = "",
    val amount: Long = 0L,
    val createdAt: Long = 0L,
    val status: String = "pending"
)
