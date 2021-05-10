package org.sopiro.chat.client

import org.sopiro.chat.server.ClientHandle
import org.sopiro.chat.server.Server
import org.sopiro.chat.utils.Resources
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultCaret


class ChatServerWindow(
    title: String,
    private val name: String,
    disposeCallBack: (port: Int) -> Unit,
    private val enterCallBack: (port: Int) -> Unit,
    private val exitCallBack: (port: Int) -> Unit,
    private val onStartCallBack: (port: Int) -> Unit
) : Server()
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

    private var memberList = Vector<Member>()

    private var port: Int = 0

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

        enterBtn = JButton(Resources.ENTER)
        enterBtn.font = Resources.font16

        cmdLine = JTextField(60)
        cmdLine.font = Resources.font16

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
                super.windowClosing(e)
                terminate()
                disposeCallBack(port)
            }
        })
        enterBtn.addActionListener {
            interpret()
        }
        cmdLine.addActionListener {
            enterBtn.doClick()
        }
        window.setLocationRelativeTo(null)

        cmdLine.requestFocus()
    }

    fun launch(port: Int)
    {
        window.isVisible = true

        addMember("me", name)

        super.start(port)
    }

    private fun interpret()
    {
        val text = cmdLine.text

        cmdLine.text = ""
        newMessage("$name: $text")
    }

    override fun onWaitClientAccess()
    {

    }

    override fun onStartServer(port: Int)
    {
        this.port = port
        onStartCallBack(port)
    }

    override fun onClientConnect(handle: ClientHandle)
    {
        enterCallBack(port)
    }

    override fun onClientDisconnect(handle: ClientHandle)
    {
        super.onClientDisconnect(handle)

        exitCallBack(port)

        val name = findName(handle.ip)

        newMessage("$name ${Resources.SOMEONE_OUT}")
        deleteMember(handle.ip)
    }

    override fun onReceiveData(handle: ClientHandle, parser: Parser)
    {
        when (parser.cmd)
        {
            "ienter" ->
            {
                val ip = handle.ip
                val name = parser.getOption("n")

                newMessage("$name ${Resources.SOMEONE_ENTER}")
                addMember(ip, name!!)
            }

            "msg" ->
            {
                val msg = parser.getOption("m")
                val name = parser.getOption("n")

                newMessage(name + ": " + msg!!)
            }

            else -> println(parser)
        }
    }

    private fun findName(ip: String): String?
    {
        for (i in memberList.indices)
        {
            val member = memberList[i]
            if (member.ip == ip) return member.name
        }

        return null
    }

    private fun deleteMember(ip: String)
    {
        for (i in memberList.indices)
        {
            val member = memberList[i]

            if (member.ip == ip)
            {
                memberList.removeElementAt(i)
                break
            }
        }

        updateNameListAndNotify()
    }

    private fun addMember(ip: String, name: String)
    {
        memberList.add(Member(ip, name))

        updateNameListAndNotify()
    }

    private fun updateNameListAndNotify()
    {
        val listModel = DefaultListModel<String>()

        memberList.forEach {
            listModel.addElement(it.name)
        }

        list.model = listModel

        val renderer = DefaultListCellRenderer()
        renderer.horizontalAlignment = JLabel.CENTER
        list.cellRenderer = renderer

        list.updateUI()

        notifyMemberUpdate()
    }

    private fun newMessage(msg: String)
    {
        screen.append(msg + "\n")
        super.sendToAll("msg -m \"$msg\"")
    }

    private fun memberInfoString(): String
    {
        var res = "|memberInfo|"

        for (i in memberList.indices)
        {
            res += memberList[i].name + "|"
        }

        return res
    }

    private fun notifyMemberUpdate()
    {
        super.sendToAll(memberInfoString())
    }
}