package org.sopiro.chat.server.room

object RoomManager
{
    private val rooms: MutableList<Room> = ArrayList()

    fun getRoomInfo(): String
    {
        var res: String = "|roomInfo|" + rooms.size.toString() + "|"

        rooms.forEach {
            res += it.ip + "|" + it.port + "|" + it.roomName + "|" + it.hostName + "|" + it.numMembers + "|"
        }

        return res
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

//    fun interpretInfo(parser: Parser): Vector<Vector<String>>
//    {
//
//    }
}