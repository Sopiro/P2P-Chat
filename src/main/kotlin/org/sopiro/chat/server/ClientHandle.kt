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
    fun release()
    {
        reader.close()
        writer.close()
    }
}