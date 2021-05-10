package org.sopiro.chat.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopiro.chat.utils.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.ConnectException
import java.net.Socket


abstract class Client()
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private var isServerOnline: Boolean = false

    fun start(serverIp: String, port: Int)
    {
        try
        {
            socket = Socket(serverIp, port)

            scope.launch {
                waitServerMessage(socket!!)
            }

        } catch (e: ConnectException)
        {
            println("Server is currently closed")
        }

        isServerOnline = socket != null

        if (isServerOnline)
        {
            writer = PrintWriter(socket!!.getOutputStream())
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        }

        onConnect(isServerOnline)
    }

    // Receives message comes from server
    private fun waitServerMessage(socket: Socket)
    {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        var message: String?

        while (true)
        {
            try
            {
                message = reader.readLine()
                if (message == null) // Server termination
                {
                    onServerClosed()
                    break
                }

                if (message != "")
                {
                    val parser = Parser(message)
                    onReceiveData(parser)
                }
            } catch (e: Exception)
            {
                when (e.message!!.toLowerCase())
                {
                    "connection reset" ->
                    {
                        System.err.println("Client: connection reset")
                        break
                    }

                    "socket closed" ->
                    {
                        System.err.println("Client: socket closed")
                        break
                    }

                    "socket is closed" -> // self termination
                    {
                        System.err.println("Client: socket is closed")
                        break
                    }

                    else -> e.printStackTrace()
                }
            }
        }
    }

    protected abstract fun onConnect(isServerOnline: Boolean)

    protected abstract fun onReceiveData(parser: Parser)

    protected abstract fun onServerClosed()

    protected fun sendToServer(message: String): Boolean
    {
        return if (isServerOnline)
        {
            writer!!.println(message)
            writer!!.flush()

            true
        } else
        {
            false
        }
    }

    fun terminate()
    {
        println("Terminate Client Program")

        if (socket != null)
            socket!!.close()
    }
}