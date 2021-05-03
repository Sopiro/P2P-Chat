package org.sopiro.chat.client

import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.net.Socket
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class ClientWindow(title: String) : JFrame(title)
{
    private var body: JPanel
    private var foot: JPanel
    private var table: JTable

    private val columnNames = Vector(listOf("방장", "방제", "인원수"))
    private var rowData: Vector<Vector<String>>? = null

    private lateinit var client: Client

    init
    {
        // JFrame settings
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE

        (contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

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
        scrollPane.setBorder(BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE)))

        body.add(scrollPane)

        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        pack()

        setLocationRelativeTo(null)

        isVisible = true

        tryStart("127.0.0.1", 1234)
    }

    private fun tryStart(serverIP: String, port: Int)
    {
        client = Client(serverIP, port)

        while (!client.readyToGo)
        {
            // Block
        }

        println("isServerOnline: ${client.isServerOnline}")

        if (client.isServerOnline)
        {
            client.sendMessage("안녕 난 클라이언트")
        } else
        {

        }
    }
}
