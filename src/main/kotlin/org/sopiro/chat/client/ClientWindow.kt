package org.sopiro.chat.client

import org.sopiro.chat.server.EnterRoomDialog
import org.sopiro.chat.server.NewRoomDialog
import org.sopiro.chat.server.room.Room
import org.sopiro.chat.server.room.RoomManager
import org.sopiro.chat.utils.FontLib
import org.sopiro.chat.utils.MyIp
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
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

    private lateinit var roomData: List<Room>

    private val serverIP = "14.38.149.139"
    private val serverPort = 1234

    private var myPort = 5678

    private var isServerOnline: Boolean = false

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Layout panels
        jpnBody = JPanel()
        jpnBody.layout = BorderLayout()
        jpnBody.border = BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE))
        jpnFoot = JPanel()
        jpnFoot.layout = FlowLayout(FlowLayout.RIGHT)

        // Body controls
        table = JTable()
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Foot controls
        btnNewRoom = JButton("방만들기")
        btnNewRoom.font = FontLib.font16
        btnEnterRoom = JButton("접속")
        btnEnterRoom.font = FontLib.font16
        btnRefresh = JButton("새로고침")
        btnRefresh.font = FontLib.font16

        // Add controls into layout panel
        jpnBody.add(JScrollPane(table))
        jpnFoot.add(btnRefresh)
        jpnFoot.add(btnNewRoom)
        jpnFoot.add(btnEnterRoom)

        // Add layout panels into window
        window.add(jpnBody, BorderLayout.CENTER)
        window.add(jpnFoot, BorderLayout.SOUTH)

        window.pack()
        window.setLocationRelativeTo(null)

        // Add listeners
        window.addWindowListener(object : WindowAdapter()
        {
            override fun windowClosing(e: WindowEvent)
            {
                super.windowClosing(e)
                terminate()
            }
        })
        btnNewRoom.addActionListener {
            NewRoomDialog(window, "방 만들기", true) { name: String, roomName: String ->
                if (name.trim().isEmpty() || roomName.trim().isEmpty())
                {
                    alert("제대로 입력해주세요")
                } else
                {
                    newRoom(myPort, name, roomName)
                }
            }
        }
        btnEnterRoom.addActionListener {
            EnterRoomDialog(window, "방 입장", true) { name: String ->
                if (table.selectedRow == -1)
                {
                    alert("방을 선택해 주세요")
                    return@EnterRoomDialog
                }

                if (name.trim().isEmpty())
                {
                    alert("제대로 입력해주세요 ")
                } else
                {
                    enterTheRoom(name)
                }
            }
        }
        btnRefresh.addActionListener {
            requestRefresh()
        }

        window.isVisible = true

        super.start(serverIP, serverPort)
    }

    override fun onConnect(isServerOnline: Boolean)
    {
        this.isServerOnline = isServerOnline
        if (!isServerOnline)
        {
            alert("마스터 서버가 닫혀있습니다.")
        }
    }

    override fun onReceiveData(parser: Parser)
    {
        when (parser.cmd)
        {
            "roomInfo" ->
            {
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
        table.font = FontLib.font16

        val centerRenderer = DefaultTableCellRenderer()
        centerRenderer.horizontalAlignment = JLabel.CENTER

        table.columnModel.getColumn(0).cellRenderer = centerRenderer
        table.columnModel.getColumn(1).cellRenderer = centerRenderer
        table.columnModel.getColumn(2).cellRenderer = centerRenderer

        table.updateUI()
        table.columnModel.getColumn(1).minWidth = 230
    }

    private fun newRoom(port: Int, name: String, roomName: String)
    {
        window.isVisible = false
        chatServerWindow = ChatServerWindow(
            roomName, name,
            {
                window.isVisible = true
                super.sendToServer("deleteRoom -p \"$it\"")
            },
            {
                super.sendToServer("rmPlus -p \"$it\"")
            },
            {
                super.sendToServer("rmMinus -p \"$it\"")
            },
            {
                super.sendToServer("newRoom -p \"$it\" -hn \"$name\" -rn \"$roomName\"")
            }

        )
        chatServerWindow.launch(port)
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
    }

    private fun requestRefresh()
    {
        if (!isServerOnline)
        {
            super.start(serverIP, serverPort)

        } else
        {
            super.sendToServer("refresh")
        }
    }

    private fun alert(message: Any?)
    {
        JOptionPane.showMessageDialog(window, message, "알림", JOptionPane.PLAIN_MESSAGE)
    }
}
