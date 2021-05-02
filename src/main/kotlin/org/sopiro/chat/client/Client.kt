package org.sopiro.chat.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopiro.chat.server.Server
import org.sopiro.chat.utils.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import javax.swing.JOptionPane

class Client(host: String, port: Int)
{
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var socket: Socket? = null

    var readyToGo: Boolean = false
        private set

    val isServerOnline: Boolean
        get()
        {
            return socket != null
        }

    init
    {
        try
        {
            socket = Socket(host, port)

            scope.launch {
                onRead(socket!!)
            }

            val write = PrintWriter(socket!!.getOutputStream())

            write.println("msg -m \"nada sex\"")
            write.flush()

        } catch (e: ConnectException)
        {
            println("Server is closed")
        }

        if (socket == null)
        {
            readyToGo = true
        }
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

                println(message)

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
                        println("Server closed")
                        terminate()
                    }
                }

                break
            }
        }

        reader.close()
        terminate()
    }

    private fun handleData(parser: Parser)
    {
        when (parser.cmd)
        {
            "roomInfo" ->
            {
                readyToGo = true
            }

            "noti" ->
            {
                JOptionPane.showMessageDialog(null, parser.getOption("m"), "notification", JOptionPane.OK_OPTION)
            }
        }
    }

    private fun terminate()
    {
        socket!!.close()
    }
}