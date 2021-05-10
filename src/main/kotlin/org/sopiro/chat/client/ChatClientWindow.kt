package org.sopiro.chat.client

import org.sopiro.chat.utils.Parser
import org.sopiro.chat.utils.Resources
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.DefaultCaret


class ChatClientWindow(
    title: String,
    private val name: String,
    disposeCallBack: () -> Unit
) : Client()
{
    private var window: JFrame = JFrame(title)
    private var body: JPanel
    private var right: JPanel
    private var foot: JPanel
    private var screen: JTextArea
    private var label: JLabel
    private var list: JList<String>
    private var enterBtn: JButton
    private var cmdLine: JTextField

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        window.iconImage = Resources.icon
        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Layout panels
        body = JPanel()
        body.layout = BorderLayout(10, 10)
        body.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        right = JPanel()
        right.layout = BorderLayout(10, 10)
        right.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        foot = JPanel()
        foot.layout = FlowLayout(FlowLayout.RIGHT)

        // Body controls
        screen = JTextArea(20, 0)
        screen.lineWrap = true
        screen.isEditable = false
        (screen.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE
        screen.font = Resources.font16

        // Foot controls
        enterBtn = JButton(Resources.ENTER)
        enterBtn.font = Resources.font16
        cmdLine = JTextField(60)
        cmdLine.font = Resources.font16

        // Right controls
        label = JLabel(Resources.MEMBERS)
        label.horizontalAlignment = JLabel.CENTER
        label.font = Resources.font16

        list = JList()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.preferredSize = Dimension(100, 0)
        list.maximumSize = Dimension(100, 1000)
        list.font = Resources.font16

        // Add controls into layout panel
        right.add(label, BorderLayout.NORTH)
        right.add(JScrollPane(list, 20, 30), BorderLayout.CENTER)
        body.add(JScrollPane(screen, 20, 30), BorderLayout.CENTER)
        foot.add(cmdLine)
        foot.add(enterBtn)

        // Add layout panels into window
        window.add(body, BorderLayout.CENTER)
        window.add(right, BorderLayout.EAST)
        window.add(foot, BorderLayout.SOUTH)

        window.pack()

        // Add listeners
        window.addWindowListener(object : WindowAdapter()
        {
            override fun windowClosing(e: WindowEvent)
            {
                terminate()
                disposeCallBack()
                super.windowClosing(e)
            }
        })
        cmdLine.addActionListener {
            enterBtn.doClick()
        }
        enterBtn.addActionListener {
            interpret()
        }

        window.setLocationRelativeTo(null)

        cmdLine.requestFocus()
    }

    fun launch(ip: String, port: Int)
    {
        window.isVisible = true

        super.start(ip, port)
    }

    private fun interpret()
    {
        val text = cmdLine.text

        cmdLine.text = ""
        sendToServer("msg -m \"$text\" -n \"$name\"")
    }

    override fun onConnect(isServerOnline: Boolean)
    {
        sendToServer("ienter -n \"$name\"")
    }

    override fun onReceiveData(parser: Parser)
    {
        when (parser.cmd)
        {
            "memberInfo" ->
            {
                val listModel = DefaultListModel<String>()

                for (i in 1 until parser.tokens.size)
                {
                    listModel.addElement(parser.tokens[i])
                }

                list.model = listModel

                val renderer = DefaultListCellRenderer()
                renderer.horizontalAlignment = JLabel.CENTER
                list.cellRenderer = renderer

                Thread.yield()
                list.updateUI()
            }

            "msg" ->
            {
                val msg = parser.getOption("m") + "\n"
                screen.append(msg)
            }
        }
    }

    override fun onServerClosed()
    {
        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
        JOptionPane.showMessageDialog(window, Resources.LEFT_ROOM, Resources.NOTICE, JOptionPane.PLAIN_MESSAGE)
    }
}