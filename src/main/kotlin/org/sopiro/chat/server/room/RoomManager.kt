package org.sopiro.chat.server.room

import org.sopiro.chat.utils.Parser


object RoomManager
{
    val rooms: MutableList<Room> = ArrayList()
    private const val roomSize = 5

    fun packIntoRoomInfoString(): String
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
    )
    {
        rooms.add(Room(ip, port, roomName, hostName, 1))
    }

    fun deleteRoom(ip: String, port: Int): Boolean
    {
        for (i in rooms.indices)
        {
            val room = rooms[i]

            if (room.ip == ip && room.port == port)
            {
                rooms.removeAt(i)
                return true
            }
        }

        return false
    }

    fun someoneEnter(ip: String, port: Int): Boolean
    {
        for (i in rooms.indices)
        {
            val room = rooms[i]

            if (room.ip == ip && room.port == port)
            {
                room.numMembers++
                return true
            }
        }

        return false
    }

    fun someoneExit(ip: String, port: Int): Boolean
    {
        for (i in rooms.indices)
        {
            val room = rooms[i]

            if (room.ip == ip && room.port == port)
            {
                room.numMembers--
                return true
            }
        }

        return false
    }


    fun interpretInfo(parser: Parser): List<Room>?
    {
        if (parser.cmd != "roomInfo") return null

        val size = Integer.parseInt(parser.tokens[1])
        val res = ArrayList<Room>()

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