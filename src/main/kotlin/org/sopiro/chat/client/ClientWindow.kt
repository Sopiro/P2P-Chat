package org.sopiro.chat.client

import org.sopiro.chat.server.ClientHandle
import org.sopiro.chat.server.Server
import org.sopiro.chat.server.room.Room
import org.sopiro.chat.server.room.RoomManager
import org.sopiro.chat.utils.Parser
import org.sopiro.chat.utils.Resources
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import kotlin.concurrent.thread


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
    private var menubar: JMenuBar

    private var columnNames = Vector(Resources.COLUMN_NAMES)

    private val rowData = Vector<Vector<String>>()
    private lateinit var roomData: List<Room>

    private var serverIP = "14.38.149.139"
    private var serverPort = 1234
    private var myPort = 5678
    private var isServerOnline: Boolean = false
    private var amIRoomable: Boolean? = null

    private val menu1: JMenu
    private val subMenu: JMenu
    private val item1: JMenuItem
    private val item2: JMenuItem
    private val item3: JMenuItem

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        window.iconImage = Resources.icon
        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        menubar = JMenuBar()
        menu1 = JMenu(Resources.SETTINGS)
        menu1.font = Resources.font12
        item1 = JMenuItem(Resources.MASTER_SERVER_SETTING)
        subMenu = JMenu(Resources.LANG_SETT)
        item2 = JMenuItem("한국어")
        item3 = JMenuItem("english")
        subMenu.font = Resources.font12
        item1.font = Resources.font12
        item2.font = Resources.font12
        item3.font = Resources.font12
        menu1.add(item1)
        subMenu.add(item2)
        subMenu.add(item3)
        menu1.add(subMenu)
        menubar.add(menu1)

        // Layout panels
        jpnBody = JPanel()
        jpnBody.layout = BorderLayout()
        jpnBody.border = BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE))
        jpnFoot = JPanel()
        jpnFoot.layout = FlowLayout(FlowLayout.RIGHT)

        // Body controls
        val tm: DefaultTableModel = object : DefaultTableModel(rowData, columnNames)
        {
            override fun isCellEditable(row: Int, column: Int): Boolean
            {
                return false
            }
        }

        table = JTable(tm)
        table.rowHeight = 30
        table.font = Resources.font16
        val centerRenderer = DefaultTableCellRenderer()
        centerRenderer.horizontalAlignment = JLabel.CENTER

        table.columnModel.getColumn(0).cellRenderer = centerRenderer
        table.columnModel.getColumn(1).cellRenderer = centerRenderer
        table.columnModel.getColumn(2).cellRenderer = centerRenderer

        table.columnModel.getColumn(1).minWidth = 230
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Foot controls
        btnNewRoom = JButton(Resources.NEW_ROOM)
        btnNewRoom.font = Resources.font16
        btnEnterRoom = JButton(Resources.ENTER_ROOM)
        btnEnterRoom.font = Resources.font16
        btnRefresh = JButton(Resources.REFRESH)
        btnRefresh.font = Resources.font16

        // Add controls into layout panel
        jpnBody.add(JScrollPane(table))
        jpnFoot.add(btnRefresh)
        jpnFoot.add(btnNewRoom)
        jpnFoot.add(btnEnterRoom)

        // Add layout panels into window
        window.jMenuBar = menubar
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
            NewRoomDialog(window, Resources.NEW_ROOM, true) { name: String, roomName: String ->
                if (name.trim().isEmpty() || roomName.trim().isEmpty())
                {
                    alert(Resources.CORRECT_PLS)
                } else
                {
                    newRoom(myPort, name, roomName)
                }
            }
        }
        btnEnterRoom.addActionListener {
            if (table.selectedRow != -1)
            {
                EnterRoomDialog(window, Resources.ENTER_ROOM, true) { name: String ->
                    if (name.trim().isEmpty())
                    {
                        alert(Resources.CORRECT_PLS)
                    } else
                    {
                        enterTheRoom(name)
                    }
                }
            } else
            {
                alert(Resources.SELECT_PLS)
            }
        }
        btnRefresh.addActionListener {
            requestRefresh()
        }

        item1.addActionListener {
            MasterServerSettingDialog(window, Resources.MASTER_SERVER_SETTING, true, serverIP, serverPort) { ip, port ->
                try
                {
                    if (ip.split(".").size != 4) throw Exception()
                    val portInt = Integer.parseInt(port)

                    serverIP = ip
                    serverPort = portInt
                    requestRefresh()
                } catch (e: Exception)
                {
                    alert(Resources.CORRECT_PLS_IP_PORT)
                }
            }
        }
        item2.addActionListener {
            Resources.language = Resources.Lang.KOR
            languageChanged()
        }
        item3.addActionListener {
            Resources.language = Resources.Lang.ENG
            languageChanged()
        }

        window.isVisible = true

        super.start(serverIP, serverPort)
    }

    private fun languageChanged()
    {
        columnNames = Vector(Resources.COLUMN_NAMES)

        val tm: DefaultTableModel = object : DefaultTableModel(rowData, columnNames)
        {
            override fun isCellEditable(row: Int, column: Int): Boolean
            {
                return false
            }
        }

        table.model = tm

        table.rowHeight = 30
        table.font = Resources.font16
        val centerRenderer = DefaultTableCellRenderer()
        centerRenderer.horizontalAlignment = JLabel.CENTER

        table.columnModel.getColumn(0).cellRenderer = centerRenderer
        table.columnModel.getColumn(1).cellRenderer = centerRenderer
        table.columnModel.getColumn(2).cellRenderer = centerRenderer

        table.columnModel.getColumn(1).minWidth = 230
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        table.updateUI()

        btnNewRoom.text = Resources.NEW_ROOM
        btnEnterRoom.text = Resources.ENTER_ROOM
        btnRefresh.text = Resources.REFRESH
        menu1.text = Resources.SETTINGS
        item1.text = Resources.MASTER_SERVER_SETTING
        subMenu.text = Resources.LANG_SETT

        window.title = Resources.ROOM_LIST
    }

    override fun onConnect(isServerOnline: Boolean)
    {
        this.isServerOnline = isServerOnline
        if (!isServerOnline)
        {
            alert(Resources.MASTER_SERVER_CLOSED)
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
                checkRoomable()
            }

            "noti" ->
            {
                alert(parser.getOption("m"))
            }

            "roomable" ->
            {
                val roomable = parser.getOption("r")

                amIRoomable = roomable == "true"

                println("Am i able to make a room?: $amIRoomable")
            }
        }
    }

    override fun onServerClosed()
    {
        println("Server closed")
        alert(Resources.MASTER_SERVER_JUST_CLOSED)
    }

    private fun reloadRoom()
    {
        rowData.clear()

        for (i in roomData.indices)
        {
            val row = Vector<String>()

            row.add(roomData[i].hostName)
            row.add(roomData[i].roomName)
            row.add(roomData[i].numMembers.toString())

            rowData.add(row)
        }

        table.updateUI()
    }

    private fun checkRoomable()
    {
        if (amIRoomable != null) return

        thread {
            val server = object : Server()
            {
                override fun onWaitClientAccess()
                {
                }

                override fun onStartServer(port: Int)
                {
                    sendToServer("checkRoomable -p \"$port\"")
                }

                override fun onClientConnect(handle: ClientHandle)
                {
                }

                override fun onReceiveData(handle: ClientHandle, parser: Parser)
                {
                }
            }

            server.start(myPort)

            while (amIRoomable == null)
            {
                Thread.yield()
            }

            server.terminate()
        }
    }

    private fun newRoom(port: Int, name: String, roomName: String)
    {
        while (amIRoomable == null)
        {
            Thread.yield()
        }

        if (!amIRoomable!!)
        {
            alert(Resources.HOST_ERR)
        } else
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
        JOptionPane.showMessageDialog(window, message, Resources.NOTICE, JOptionPane.PLAIN_MESSAGE)
    }
}
