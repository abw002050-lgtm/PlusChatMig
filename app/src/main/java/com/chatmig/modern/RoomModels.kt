package com.chatmig.modern

data class RoomMember(
    val uid: String = "",
    val name: String = "",
    val role: String = "member",
    val joinedAt: Long = 0L,
    val banned: Boolean = false
)

data class ChatRoomModel(
    val roomId: String = "",
    val name: String = "",
    val topic: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L,
    val raised: Boolean = false,
    val memberCount: Long = 0L
)
