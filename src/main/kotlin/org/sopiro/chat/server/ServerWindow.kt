package org.sopiro.chat.server

import org.sopiro.chat.client.Client
import org.sopiro.chat.server.room.RoomManager
import org.sopiro.chat.utils.FontLib
import org.sopiro.chat.utils.Logger
import org.sopiro.chat.utils.MyIp
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.DefaultCaret
import kotlin.concurrent.thread


class ServerWindow(title: String) : Server()
{
    private var window: JFrame = JFrame(title)
    private var body: JPanel
    private var foot: JPanel
    private var screen: JTextArea
    private var enterBtn: JButton
    private var cmdLine: JTextField

    private var logger: Logger

    private val defaultMsg = "start -p 1234"

    private var port: Int? = null

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Layout panels
        body = JPanel()
        body.layout = BorderLayout()
        body.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)

        foot = JPanel()
        foot.layout = FlowLayout(FlowLayout.RIGHT)

        // Body controls
        screen = JTextArea(20, 0)
        screen.lineWrap = true
        screen.isEditable = false
        (screen.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE
        screen.font = FontLib.font16

        // Foot controls
        enterBtn = JButton("입력")
        enterBtn.font = FontLib.font12

        cmdLine = JTextField(60)
        cmdLine.font = FontLib.font16
        cmdLine.text = defaultMsg

        // Add controls into layout panel
        body.add(JScrollPane(screen, 20, 30), BorderLayout.CENTER)
        foot.add(cmdLine)
        foot.add(enterBtn)

        // Add layout panels into window
        window.add(body, BorderLayout.CENTER)
        window.add(foot, BorderLayout.SOUTH)

        window.pack()

        // Add listeners
        window.addWindowListener(object : WindowAdapter()
        {
            override fun windowClosing(e: WindowEvent)
            {
                super.windowClosing(e)
                terminate()
            }
        })
        cmdLine.addActionListener {
            enterBtn.doClick()
        }
        enterBtn.addActionListener {
            interpret()
        }

        window.setLocationRelativeTo(null)

        logger = Logger(screen)
        cmdLine.requestFocus()

        window.isVisible = true
    }

    private fun interpret()
    {
        val rawText = cmdLine.text
        val parser = Parser(rawText)

        when (parser.cmd)
        {
            "start" ->
            {
                try
                {
                    port = Integer.parseInt(parser.getOption("p"))
                    if (!super.start(port!!))
                    {
                        logger.log("Server is already Online")
                    }
                } catch (e: NumberFormatException)
                {
                    logger.log("Sets port with -p option correctly")
                }
            }

            "error" ->
            {
                logger.log("Error: $rawText")
            }

            "ls" ->
            {
                logger.log("$numClients Clients are existing, ${RoomManager.howMany()} Rooms exist")
            }

            "rl" ->
            {
                logger.log("Room lists")

                RoomManager.rooms.forEach {
                    logger.logNoTime("ip:${it.ip} | port: ${it.port} | hostName: ${it.hostName} | roomName: ${it.roomName} | numMembers: ${it.numMembers}")
                }
            }

            "cls", "clear" ->
            {
                screen.text = ""
            }

            "noti" ->
            {
                if (parser.getOption("m") == null) return

                val msg = parser.getOption("m").toString()
                notifyToAll(msg)
                logger.log("Notified to all clients: $msg")
            }

            "ip" ->
            {
                thread {
                    logger.log(MyIp.ip())
                }
            }

            "port" ->
            {
                logger.log(port!!.toString())
            }

            "help" ->
            {
                logger.log("help")
                logger.logNoTime("-------------------------------------------------")
                logger.logNoTime("[start] -> Start a server program")
                logger.logNoTime("    usage: start -p \"port\"")
                logger.logNoTime("[ls] -> Show user, room status")
                logger.logNoTime("    usage: ls")
                logger.logNoTime("[rl] -> Show room list")
                logger.logNoTime("    usage: rl")
                logger.logNoTime("[cls, clear] -> Clear screen")
                logger.logNoTime("    usage: cls, clear")
                logger.logNoTime("[noti] -> Notify a message to all clients")
                logger.logNoTime("    usage: noti -m \"message\"")
                logger.logNoTime("[ip] -> show your ip")
                logger.logNoTime("    ip:")
                logger.logNoTime("[port] -> show port that program run on")
                logger.logNoTime("    port:")
                logger.logNoTime("[help] -> Show this")
                logger.logNoTime("    usage: help")
                logger.logNoTime("[exit] -> End server program")
                logger.logNoTime("    usage: exit")
                logger.logNoTime("-------------------------------------------------")
            }

            "exit" ->
            {
                terminate()
                window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            }

            else -> logger.log(rawText)
        }

        cmdLine.text = ""
    }

    override fun onStartServer(port: Int)
    {
        logger.log("Server started on : $port")
    }

    override fun onWaitClientAccess()
    {
        logger.log("Waiting client's access")
    }

    override fun onClientConnect(handle: ClientHandle)
    {
        logger.log("Got one ${handle.ip}")

        // Send room info
        sendRoomInfo(handle)
    }

    override fun onReceiveData(handle: ClientHandle, parser: Parser)
    {
        when (parser.cmd)
        {
            "msg" ->
            {
                logger.log(parser.getOption("m").toString())
            }

            "checkRoomable" ->
            {
                val ip = handle.ip
                val port = Integer.parseInt(parser.getOption("p"))

                if (tryConnect(ip, port))
                {
                    send(handle, "roomable -r true")
                } else
                {
                    send(handle, "roomable -r false")
                }
            }

            "newRoom" ->
            {
                val ip = handle.ip
                val port = Integer.parseInt(parser.getOption("p"))
                val rn = parser.getOption("rn")
                val hn = parser.getOption("hn")

                RoomManager.newRoom(ip, port, rn!!, hn!!)

                sendRoomInfoToAll()

                logger.log("${handle.ip} requests new room")
                logger.logNoTime("RoomName: $rn")
                logger.logNoTime("HostName: $hn")
            }

            "deleteRoom" ->
            {
                val ip = handle.ip
                val port = Integer.parseInt(parser.getOption("p"))

                RoomManager.deleteRoom(ip, port)

                sendRoomInfoToAll()
                logger.log("$ip deletes room")
            }

            "rmPlus" ->
            {
                val ip = handle.ip
                val port = Integer.parseInt(parser.getOption("p"))

                RoomManager.someoneEnter(ip, port)
                sendRoomInfoToAll()
            }

            "rmMinus" ->
            {
                val ip = handle.ip
                val port = Integer.parseInt(parser.getOption("p"))

                RoomManager.someoneExit(ip, port)
                sendRoomInfoToAll()
            }

            "refresh" ->
            {
                sendRoomInfo(handle)
                logger.log("${handle.ip} requests refresh")
            }

            else -> logger.log(parser.str)
        }
    }

    override fun onClientDisconnect(handle: ClientHandle)
    {
        logger.log("${handle.ip} goes out")

        super.onClientDisconnect(handle)
    }

    private fun tryConnect(ip: String, port: Int): Boolean
    {
        var res = false

        val t = thread {
            val test = object : Client()
            {
                override fun onConnect(isServerOnline: Boolean)
                {
                    res = isServerOnline
                }

                override fun onReceiveData(parser: Parser)
                {
                }

                override fun onServerClosed()
                {
                }
            }

            test.start(ip, port)
        }

        t.join()

        return res
    }

    private fun notifyToAll(message: String)
    {
        super.sendToAll("noti -m \"$message\"")
    }

    private fun sendRoomInfo(handle: ClientHandle)
    {
        send(handle, RoomManager.packIntoRoomInfoString())
    }

    private fun sendRoomInfoToAll()
    {
        sendToAll(RoomManager.packIntoRoomInfoString())
    }
}
