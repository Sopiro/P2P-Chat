package org.sopiro.chat.server.room

import org.sopiro.chat.utils.Parser
import java.util.*
import kotlin.collections.ArrayList

object RoomManager
{
    private val rooms: MutableList<Room> = ArrayList()
    private const val roomSize = 5

    fun getRoomInfo(): String
    {
        var res: String = "|roomInfo|" + rooms.size.toString() + "|"

        rooms.forEach {
            res += it.ip + "|" + it.port + "|" + it.roomName + "|" + it.hostName + "|" + it.numMembers + "|"
        }

        return res
    }

    fun howMany(): Int
    {
        return rooms.size
    }

    fun newRoom(
        ip: String,
        port: Int,
        roomName: String,
        hostName: String,
        numMembers: Int
    )
    {
        rooms.add(Room(ip, port, roomName, hostName, numMembers))
    }

    fun interpretInfo(parser: Parser): List<Room>?
    {
        if (parser.cmd != "roomInfo") return null

        val size = Integer.parseInt(parser.tokens[1])
        val res = ArrayList<Room>()
        var row: Vector<String>

        for (i in 0 until size)
        {
            val room = Room(
                parser.tokens[2 + i * roomSize + 0],
                Integer.parseInt(parser.tokens[2 + i * roomSize + 1]),
                parser.tokens[2 + i * roomSize + 2],
                parser.tokens[2 + i * roomSize + 3],
                Integer.parseInt(parser.tokens[2 + i * roomSize + 4]),
            )

            res.add(room)
        }

        return res
    }
}