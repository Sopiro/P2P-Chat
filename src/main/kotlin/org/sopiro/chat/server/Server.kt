package org.sopiro.chat.server

import kotlinx.coroutines.*
import org.sopiro.chat.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class Server(private val port: Int, private val logger: Logger)
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private val ss: ServerSocket = ServerSocket(port)
    val clients: MutableList<Socket> = ArrayList()

    fun start()
    {
        logger.log("Server started on port: $port")

        while (true)
        {
            logger.log("Waiting client's access")
            Thread.sleep(10)

            try
            {
                val socket = ss.accept()
                clients.add(socket)
                logger.log("Got one ${socket.localAddress}")

                scope.launch {
                    val reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    while (true)
                    {
                        var message: String
                        if (reader.readLine().also { message = it } != null)
                        {
                            logger.log(message)
                            continue
                        }
                    }
                }

            } catch (e: SocketException)
            {
                break
            }
        }
    }

    fun terminate()
    {
        println("Terminate Server")

        ss.close()
        clients.forEach {
            it.close()
        }
    }
}