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
import java.net.SocketException

open class Server()
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var serverSocket: ServerSocket? = null
    private val clients: MutableList<ClientHandle> = ArrayList()

    private lateinit var logger: Logger

    protected val numClients: Int
        get()
        {
            return clients.size
        }

    protected fun start(port: Int, logger: Logger)
    {
        this.logger = logger

        try
        {
            serverSocket = ServerSocket(port)
            logger.log("Server started on port: $port")

            scope.launch {
                waitClientForever()
            }

        } catch (e: SocketException)
        {
            println(e)
        }


    }

    private fun waitClientForever()
    {
        while (true)
        {
            logger.log("Waiting client's access")
            Thread.sleep(10)

            try
            {
                val socket = serverSocket!!.accept()


                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream())

                scope.launch {
                    handleClient(ClientHandle(socket, reader, writer))
                }

            } catch (e: SocketException)
            {
                e.printStackTrace()
            }
        }
    }

    // Handle individual client
    private fun handleClient(handle: ClientHandle)
    {
        clients.add(handle)

        onClientEnter(handle)

        var message: String?

        while (true)
        {
            try
            {
                message = handle.reader.readLine()
                if (message == null) break

                if (message != "")
                {
                    val parser = Parser(message)
                    onReceiveData(handle, parser)
                }
            } catch (e: SocketException)
            {
                when (e.message)
                {
                    "Connection reset" ->
                    {
                        break
                    }

                    else -> e.printStackTrace()
                }
            }
        }

        onClientDisconnect(handle)

    }

    protected open fun onClientEnter(handle: ClientHandle)
    {
    }

    protected open fun onClientDisconnect(handle: ClientHandle)
    {
        handle.release()
        clients.remove(handle)
    }

    protected open fun onReceiveData(handle: ClientHandle, parser: Parser)
    {
    }

    protected fun send(handle: ClientHandle, message: String)
    {
        handle.writer.println(message)
        handle.writer.flush()
    }

    protected fun sendToAll(message: String)
    {
        clients.forEach {
            send(it, message)
        }
    }

    protected fun terminate()
    {
        println("Terminate Server")

        serverSocket!!.close()
    }
}