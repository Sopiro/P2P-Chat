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

abstract class Client
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    protected var isServerOnline: Boolean = false
        private set

    protected fun start(serverIp: String, port: Int): Boolean
    {
        try
        {
            socket = Socket(serverIp, port)

            scope.launch {
                handleServer(socket!!)
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

        return isServerOnline
    }

    protected fun sendToServer(message: String)
    {
        if (isServerOnline)
        {
            writer!!.println(message)
            writer!!.flush()
        }
    }

    // Receives message comes from master server
    private fun handleServer(socket: Socket)
    {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        var message: String?

        while (true)
        {
            try
            {
                message = reader.readLine()
                if (message == null) break

                println("Got Message: $message")

                if (message != "")
                {
                    val parser = Parser(message)
                    handleData(parser)
                }
            } catch (e: Exception)
            {
                when (e.message)
                {
                    "Connection reset" ->
                    {
                        println("Server closed")
                        break
                    }
                }

                break
            }
        }

        isServerOnline = false
    }

    protected abstract fun handleData(parser: Parser)

    protected fun terminate()
    {
        println("Terminate program")

        if (isServerOnline)
        {
            writer!!.close()
            reader!!.close()
        }
    }
}