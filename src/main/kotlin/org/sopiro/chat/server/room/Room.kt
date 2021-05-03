package org.sopiro.chat.server.room

data class Room(
    val ip: String,
    val port: Int,
    val roomName: String,
    val hostName: String,
    val numMembers: Int
)
