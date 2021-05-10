package org.sopiro.chat.server

import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket


data class ClientHandle(
    val socket: Socket,
    val reader: BufferedReader,
    val writer: PrintWriter
)
{
    val ip: String
        get()
        {
            return socket.inetAddress.hostAddress
        }

    fun release()
    {
        socket.close()
//        reader.close()
//        writer.close()
        //reader and writer will be closed along with the socket closing
    }
}