package org.sopiro.chat.client

import org.sopiro.chat.server.EnterRoomDialog
import org.sopiro.chat.server.NewRoomDialog
import org.sopiro.chat.server.room.Room
import org.sopiro.chat.server.room.RoomManager
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class ClientWindow(title: String) : Client()
{
    private val window: JFrame = JFrame(title)
    private lateinit var chatServerWindow: ChatServerWindow
    private lateinit var chatClientWindow: ChatClientWindow

    private var jpnBody: JPanel
    private var jpnFoot: JPanel
    private var table: JTable
    private var btnNewRoom: JButton
    private var btnEnterRoom: JButton
    private var btnRefresh: JButton

    private val columnNames = Vector(listOf("방장", "방제", "인원수"))
    private val font = Font("serif", Font.PLAIN, 16)

    private lateinit var roomData: List<Room>

    private val serverIP = "172.18.48.1"
    private val serverPort = 1234

    private val myPort = 12345

    private var readyToGo: Boolean = false

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        jpnBody = JPanel()
        jpnBody.layout = BorderLayout()

        jpnFoot = JPanel()
        jpnFoot.layout = FlowLayout(FlowLayout.RIGHT)

        table = JTable()
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val scrollPane = JScrollPane(table)
        scrollPane.border = BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE))

        btnNewRoom = JButton("방만들기")
        btnNewRoom.addActionListener {
            NewRoomDialog(window, "방 만들기", true) { name: String, roomName: String ->
                if (name.length + roomName.length < 2)
                {
                    alert("제대로 입력해주세요")
                } else
                {
                    newRoom(myPort, name, roomName)
                }
            }
        }
        btnEnterRoom = JButton("접속")
        btnEnterRoom.addActionListener {
            EnterRoomDialog(window, "방 입장", true) { name: String ->
                if (name.isEmpty())
                {
                    alert("제대로 입력해주세요 ")
                } else
                {
                    enterTheRoom(name)
                }
            }
        }

        btnRefresh = JButton("새로고침")
        btnRefresh.addActionListener {
            requestRefresh()
        }

        jpnBody.add(scrollPane)
        jpnFoot.add(btnNewRoom)
        jpnFoot.add(btnEnterRoom)

        window.add(jpnBody, BorderLayout.CENTER)
        window.add(jpnFoot, BorderLayout.SOUTH)

        window.pack()

        window.setLocationRelativeTo(null)

        window.addWindowListener(object : WindowAdapter()
        {
            override fun windowClosing(e: WindowEvent)
            {
                terminate()
                super.windowClosing(e)
            }
        })

        window.isVisible = true

        super.start(serverIP, serverPort)
    }

    override fun onConnect(isServerOnline: Boolean)
    {
        if (isServerOnline)
        {
            sendMessage("안녕 서버")
        } else
        {
            alert("Server is currently closed")
        }

        while (!readyToGo)
        {
            Thread.yield()
        }

        sendMessage("안녕 난 클라이언트")
    }

    override fun onReceiveData(parser: Parser)
    {
//        println("Got Message: ${parser.str}")

        when (parser.cmd)
        {
            "roomInfo" ->
            {
                readyToGo = true

                roomData = RoomManager.interpretInfo(parser)!!
                reloadRoom()
            }

            "noti" ->
            {
                alert(parser.getOption("m"))
            }
        }
    }

    override fun onServerClosed()
    {
        println("Server closed")
        alert("Server closed")
    }

    private fun reloadRoom()
    {
        val rowData = Vector<Vector<String>>()

        for (i in roomData.indices)
        {
            val row = Vector<String>()

            row.add(roomData[i].hostName)
            row.add(roomData[i].roomName)
            row.add(roomData[i].numMembers.toString())

            rowData.add(row)
        }

        val dtm: DefaultTableModel = object : DefaultTableModel(rowData, columnNames)
        {
            override fun isCellEditable(row: Int, column: Int): Boolean
            {
                return false
            }
        }

        table.model = dtm
        table.rowHeight = 30
        table.font = font

        table.updateUI()
        table.columnModel.getColumn(1).minWidth = 230
    }

    private fun sendMessage(message: String)
    {
        super.sendToServer("msg -m \"$message\"")
    }

    private fun newRoom(port: Int, name: String, roomName: String)
    {
        window.isVisible = false
        chatServerWindow = ChatServerWindow(roomName, name) {
            window.isVisible = true
            super.sendToServer("deleteRoom")
        }
        chatServerWindow.launch(port)
        super.sendToServer("newRoom -p \"$port\" -hn \"$name\" -rn \"$roomName\"")
    }

    private fun enterTheRoom(myName: String)
    {
        window.isVisible = false

        val selectedRoom = roomData[table.selectedRow]

        chatClientWindow = ChatClientWindow(selectedRoom.roomName, myName) {
            window.isVisible = true
        }
        val ip = selectedRoom.ip
        val port = selectedRoom.port

        chatClientWindow.launch(ip, port)
        super.sendToServer("enterRoom -ip \"$ip\" -p \"$port\"")
    }

    private fun requestRefresh()
    {
        super.sendToServer("refresh")
    }

    private fun alert(message: Any?)
    {
        JOptionPane.showMessageDialog(window, message, "notification", JOptionPane.PLAIN_MESSAGE)
    }
}
