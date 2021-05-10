package org.sopiro.chat.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopiro.chat.utils.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ServerSocket
import java.net.SocketException


abstract class Server()
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private var serverSocket: ServerSocket? = null
    protected val clients: MutableList<ClientHandle> = ArrayList()


    protected val numClients: Int
        get()
        {
            return clients.size
        }

    private var isStarted: Boolean = false

    fun start(port: Int): Boolean
    {
        if (isStarted) return false
        isStarted = true

        var portTry = port

        while (true)
        {
            try
            {
                serverSocket = ServerSocket(portTry)

                onStartServer(portTry)

                scope.launch {
                    waitClientForever()
                }

                break
            } catch (e: SocketException)
            {
                if (e.message!!.toLowerCase().startsWith("address already in use", 0))
                {
                    portTry++
                }
                isStarted = false
            }
        }
        return isStarted
    }

    private fun waitClientForever()
    {
        while (true)
        {
            Thread.sleep(10)
            onWaitClientAccess()

            try
            {
                val socket = serverSocket!!.accept()
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream())

                scope.launch {
                    handleClient(ClientHandle(socket, reader, writer))
                }

            } catch (e: Exception)
            {
                when (e.message!!.toLowerCase())
                {
                    "connection reset" ->
                    {
                        System.err.println("Server: connection reset")
                        break
                    }

                    "socket is closed" -> // self termination
                    {
                        System.err.println("Server: socket is closed")
                        break
                    }

                    "socket closed" ->
                    {
                        System.err.println("Server: socket closed")
                        break
                    }
                }

                e.printStackTrace()
            }
        }
    }

    // Handle individual client
    private fun handleClient(handle: ClientHandle)
    {
        clients.add(handle)

        onClientConnect(handle)

        var message: String?

        while (true)
        {
            try
            {
                message = handle.reader.readLine()
                if (message == null)
                {
                    onClientDisconnect(handle)
                    break
                }

                if (message != "")
                {
                    val parser = Parser(message)
                    onReceiveData(handle, parser)
                }
            } catch (e: SocketException)
            {
                when (e.message!!.toLowerCase())
                {
                    "connection reset" ->
                    {
                        e.printStackTrace()
                        break
                    }
                    "socket closed" ->
                    {
                        System.err.println("socket closed")
                        break
                    }

                    else -> e.printStackTrace()
                }
            }
        }
    }

    protected abstract fun onWaitClientAccess()

    protected abstract fun onStartServer(port: Int)

    protected abstract fun onClientConnect(handle: ClientHandle)

    protected abstract fun onReceiveData(handle: ClientHandle, parser: Parser)

    protected open fun onClientDisconnect(handle: ClientHandle)
    {
        handle.release()
        clients.remove(handle)
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

    open fun terminate()
    {
        println("Terminate Server Program")

        if (serverSocket != null)
        {
            clients.forEach {
                it.release()
            }
            serverSocket!!.close()
        }
    }
}