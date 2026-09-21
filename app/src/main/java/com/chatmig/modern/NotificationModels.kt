package com.chatmig.modern

data class AppNotification(
    var id: String = "",
    var type: String = "",
    var title: String = "",
    var body: String = "",
    var senderId: String = "",
    var targetId: String = "",
    var timestamp: Long = 0L,
    var read: Boolean = false
)
