package com.chatmig.modern

data class RelationshipUser(val uid:String="", val name:String="", val photo:String="", val country:String="", val bio:String="", val followersCount:Int=0, val followingCount:Int=0)

data class Comment(val id:String="", val authorId:String="", val authorName:String="", val text:String="", val timestamp:Long=0L)
