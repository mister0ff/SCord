package com.discord2.app.models

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromNick: String = "",
    val fromHandle: String = "",
    val toUid: String = "",
    val status: String = "pending", // pending | accepted | rejected
    val createdAt: Long = 0L
)
