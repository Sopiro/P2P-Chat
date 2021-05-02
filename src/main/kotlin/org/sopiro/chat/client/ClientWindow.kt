package org.sopiro.chat.client

import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.util.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class ClientWindow(title: String, client: Client) : JFrame(title)
{
    private var body: JPanel
    private var foot: JPanel
    private lateinit var table: JTable

    private val columnNames = Vector(listOf("qweqw", "Title", "asd"))
    private val rowData: Vector<Vector<String>>? = null

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

        if (!client.isServerOnline)
        {
            val label = JLabel("Server closed")
            body.add(label, BorderLayout.CENTER)
        } else
        {
            val dtm: DefaultTableModel = object : DefaultTableModel(10, 3)
            {
                override fun isCellEditable(row: Int, column: Int): Boolean
                {
                    return false
                }
            }

            table = JTable(dtm)
            val scrollPane = JScrollPane(table)
            scrollPane.setBorder(BorderFactory.createMatteBorder(5, 0, 5, 0, Color(0xEEEEEE)))

            body.add(scrollPane)
        }


        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        pack()

        setLocationRelativeTo(null)

        isVisible = true
    }
}
