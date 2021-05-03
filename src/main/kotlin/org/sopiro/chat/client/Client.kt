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
import java.net.SocketException
import javax.swing.JOptionPane
import javax.swing.JOptionPane.PLAIN_MESSAGE

class Client(host: String, port: Int)
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    var readyToGo: Boolean = false
        private set

    var isServerOnline: Boolean = false

    init
    {
        try
        {
            socket = Socket(host, port)

            scope.launch {
                onRead(socket!!)
            }

        } catch (e: ConnectException)
        {
            println("Server is currently closed")
        }

        if (socket == null)
        {
            isServerOnline = false
            readyToGo = true
        }
    }

    private fun sendToMasterServer(message: String)
    {
        writer!!.println(message)
        writer!!.flush()
    }

    fun sendMessage(message: String)
    {
        sendToMasterServer("msg -m \"$message\"")
    }

    private fun onRead(socket: Socket)
    {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

        var message: String

        while (true)
        {
            try
            {
                message = reader.readLine()

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
                        terminate()
                    }

                    else -> e.printStackTrace()
                }

                break
            }
        }

        isServerOnline = false
    }

    private fun handleData(parser: Parser)
    {
        when (parser.cmd)
        {
            "roomInfo" ->
            {
                writer = PrintWriter(socket!!.getOutputStream())
                isServerOnline = true
                readyToGo = true
            }

            "noti" ->
            {
                JOptionPane.showMessageDialog(null, parser.getOption("m"), "notification", PLAIN_MESSAGE)
            }
        }
    }

    private fun terminate()
    {
        println("Terminate program")

        socket!!.close()
    }
}