package org.sopiro.chat.client

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
    private var body: JPanel
    private var foot: JPanel
    private var table: JTable

    private val columnNames = Vector(listOf("방장", "방제", "인원수"))
    private val font = Font("serif", Font.PLAIN, 16)

    private lateinit var roomData: List<Room>
        private set

    private var readyToGo: Boolean = false

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        body = JPanel()
        body.layout = BorderLayout()

        foot = JPanel()
        foot.layout = FlowLayout(FlowLayout.RIGHT)

//        val rowData = Vector<Vector<String>>()
//
//        val ele = Vector<String>()
//        ele.add("로")
//        ele.add("딩")
//        ele.add("중")
//
//        rowData.addElement(ele)

//        val dtm: DefaultTableModel = object : DefaultTableModel()
//        {
//            override fun isCellEditable(row: Int, column: Int): Boolean
//            {
//                return false
//            }
//        }

        table = JTable()

//        reloadRoom()

        val scrollPane = JScrollPane(table)
        scrollPane.border = BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE))

        body.add(scrollPane)

        window.add(body, BorderLayout.CENTER)
        window.add(foot, BorderLayout.SOUTH)

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

        super.start("127.0.0.1", 1234)
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

    private fun alert(message: Any?)
    {
        JOptionPane.showMessageDialog(window, message, "notification", JOptionPane.PLAIN_MESSAGE)
    }

}
