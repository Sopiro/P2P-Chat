package org.sopiro.chat.client

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
    private var rowData: Vector<Vector<String>>? = null

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

        rowData = Vector<Vector<String>>()

        val ele = Vector<String>()
        ele.add("a")
        ele.add("b")
        ele.add("c")

        rowData!!.addElement(ele)

        val dtm: DefaultTableModel = object : DefaultTableModel(rowData, columnNames)
        {
            override fun isCellEditable(row: Int, column: Int): Boolean
            {
                return false
            }
        }

        table = JTable(dtm)
        table.font = Font("맑은고딕", Font.PLAIN, 30)
        table.rowHeight = 30

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

        tryStart("127.0.0.1", 1234)
    }

    private fun tryStart(serverIP: String, port: Int)
    {
        super.start(serverIP, port)

        if (isServerOnline)
        {
            sendMessage("안녕 서버")
        } else
        {

        }

        while (!readyToGo)
        {
            Thread.yield()
        }

        sendMessage("안녕 난 클라이언트")
    }

    override fun onReceiveData(parser: Parser)
    {
        when (parser.cmd)
        {
            "roomInfo" ->
            {
                readyToGo = true
            }

            "noti" ->
            {
                JOptionPane.showMessageDialog(null, parser.getOption("m"), "notification", JOptionPane.PLAIN_MESSAGE)
            }
        }
    }

    private fun sendMessage(message: String)
    {
        super.sendToServer("msg -m \"$message\"")
    }

}
