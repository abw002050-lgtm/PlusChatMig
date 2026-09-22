package com.chatmig.modern

data class ChatUser(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val country: String = "",
    val online: Boolean = false,
    val level: Int = 0,
    val themeColor: String = "#6750A4",
    val lastSeen: Long = 0L
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val text: String = "",
    val type: String = "text",
    val mediaUrl: String = "",
    val assetName: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent",
    val receiverId: String = "",
    val replyToId: String = "",
    val replyToText: String = "",
    val replyToSender: String = "",
    val deletedForEveryone: Boolean = false,
    val deletedFor: List<String> = emptyList()
)

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class ChatRoomModel(
    val roomId: String = "",
    val name: String = "",
    val topic: String = "",
    val ownerId: String = "",
    val memberCount: Int = 0,
    val createdAt: Long = 0L,
    val photoUrl: String = "",
    val isPublic: Boolean = true
)

data class RoomMember(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val role: String = "member",
    val joinedAt: Long = 0L
)

data class Merchant(
    val merchantId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0L,
    val active: Boolean = true
)

data class PointTransaction(
    val id: String = "",
    val fromId: String = "",
    val toId: String = "",
    val amount: Long = 0L,
    val type: String = "",
    val merchantId: String = "",
    val merchantName: String = "",
    val status: String = "",
    val createdAt: Long = 0L
)

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    val timestamp: Long = 0L
)
