package com.chatmig.modern

data class ChatUser(val uid:String="", val name:String="", val photo:String="", val country:String="", val level:Int=0, val online:Boolean=false, val fcmToken:String="")
data class ChatMessage(
    val id:String="", val senderId:String="", val senderName:String="", val text:String="",
    val type:String="text", val mediaUrl:String="", val assetName:String="", val timestamp:Long=0L,
    val status:String="sent", val receiverId:String=""
)
data class ChatRoom(val id:String="", val name:String="", val ownerId:String="", val members:Int=0, val topic:String="")
