package org.sopiro.chat.client

import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultCaret


class ChatClientWindow(title: String, private val name: String, disposeCallBack: () -> Unit) : Client()
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

    private var listData = Vector<String>()

    init
    {
        // JFrame settings
        window.isResizable = false
        window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        (window.contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Define controls
        body = JPanel()
        body.layout = BorderLayout(10, 10)
        body.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        right = JPanel()
        right.layout = BorderLayout(10, 10)
        right.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        foot = JPanel()
        foot.layout = FlowLayout(FlowLayout.RIGHT)

        screen = JTextArea(30, 60)

        screen.lineWrap = true
        screen.isEditable = false
        (screen.caret as DefaultCaret).updatePolicy = DefaultCaret.ALWAYS_UPDATE

        enterBtn = JButton("입력")
        enterBtn.addActionListener {
            interpret()
        }

        cmdLine = JTextField(80)
        cmdLine.addActionListener {
            enterBtn.doClick()
        }

        label = JLabel("참가자")
        label.horizontalAlignment = JLabel.CENTER

        list = JList(listData)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        list.preferredSize = Dimension(100, 0)

        right.add(label, BorderLayout.NORTH)
        right.add(JScrollPane(list, 20, 30), BorderLayout.CENTER)
        body.add(JScrollPane(screen, 20, 30), BorderLayout.CENTER)
        foot.add(cmdLine)
        foot.add(enterBtn)

        window.add(body, BorderLayout.CENTER)
        window.add(right, BorderLayout.EAST)
        window.add(foot, BorderLayout.SOUTH)

        window.pack()

        window.addWindowListener(object : WindowAdapter()
        {
            override fun windowClosing(e: WindowEvent)
            {
                terminate()
                disposeCallBack()
                super.windowClosing(e)
            }
        })

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
        println("방 입장 성공")
        sendToServer("ienter -n \"$name\"")
    }

    override fun onReceiveData(parser: Parser)
    {
        when (parser.cmd)
        {
            "memberInfo" ->
            {
                println(parser)

                listData.clear()

                for (i in 1 until parser.tokens.size)
                {
                    listData.add(parser.tokens[i])
                }

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
        window.dispose()
    }
}