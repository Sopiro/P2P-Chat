package org.sopiro.chat

import java.io.BufferedWriter
import java.io.PrintWriter
import java.net.Socket

fun main()
{
    val soc = Socket("127.0.0.1", 1234)

    val write: PrintWriter = PrintWriter(soc.getOutputStream())

    write.println("Hello can you hear me?")
    write.flush()

    Thread.sleep(100)
}