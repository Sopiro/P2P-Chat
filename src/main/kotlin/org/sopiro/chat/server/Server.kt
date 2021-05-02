package org.sopiro.chat.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopiro.chat.utils.Logger
import org.sopiro.chat.utils.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class Server(private val port: Int, private val logger: Logger)
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var serverSocket: ServerSocket? = null
    private val clients: MutableList<Socket> = ArrayList()

    val numClients: Int
        get()
        {
            return clients.size
        }

    init
    {
        try
        {
            serverSocket = ServerSocket(port)
        } catch (e: SocketException)
        {
            println(e)
        }
        RoomManager.newRoom("127.0.0.1", 1234, "test", "admin", 10)
    }

    fun start()
    {
        logger.log("Server started on port: $port")

        while (true)
        {
            logger.log("Waiting client's access")
            Thread.sleep(10)

            try
            {
                val socket = serverSocket!!.accept()

                clients.add(socket)
                logger.log("Got one ${socket.localAddress}")

                scope.launch {
                    handleClient(socket)
                }

            } catch (e: SocketException)
            {
                e.printStackTrace()
                break
            }
        }
    }

    private fun handleClient(socket: Socket)
    {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        // Send RoomInfo
        send(socket, RoomManager.getRoomInfo())

        var message: String

        while (true)
        {
            try
            {
                message = reader.readLine()

                if (message != "")
                {
                    val parser = Parser(message)
                    handleData(parser)
                }
            } catch (e: SocketException)
            {
                when (e.message)
                {
                    "Connection reset" ->
                    {
                        clients.remove(socket)
                        logger.log("${socket.inetAddress} goes out")
                    }
                }
                break
            }
        }

        reader.close()
    }

    private fun handleData(parser: Parser)
    {
        when (parser.cmd)
        {
            "msg" ->
            {
                logger.log(parser.getOption("m").toString())
            }
        }
    }

    private fun send(socket: Socket, message: String)
    {
        val writer = PrintWriter(socket.getOutputStream())
        writer.println(message)
        writer.flush()
    }

    private fun sendToAll(message: String)
    {
        clients.forEach {
            send(it, message)
        }
    }

    fun notifyToAll(message: String)
    {
        sendToAll("noti -m \"$message\"")
    }

    fun terminate()
    {
        println("Terminate Server")

        serverSocket!!.close()
        clients.forEach {
            it.close()
        }
    }
}