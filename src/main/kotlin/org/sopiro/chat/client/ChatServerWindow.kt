package org.sopiro.chat.client

import org.sopiro.chat.server.ClientHandle
import org.sopiro.chat.server.Server
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.text.DefaultCaret


class ChatServerWindow(title: String, val name: String, disposeCallBack: () -> Unit) : Server()
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
                onDestroy()
                disposeCallBack()
                super.windowClosing(e)
            }
        })

        window.setLocationRelativeTo(null)

        cmdLine.requestFocus()
    }

    fun launch(port: Int)
    {
        window.isVisible = true

        newMember("me", name)

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
        println("서버 생성 성공")
    }

    override fun onClientConnect(handle: ClientHandle)
    {
        println("한명 들어옴")
    }

    override fun onClientDisconnect(handle: ClientHandle)
    {
        super.onClientDisconnect(handle)

        deleteMember(handle.socket.inetAddress.hostAddress)
    }

    override fun onReceiveData(handle: ClientHandle, parser: Parser)
    {
        when (parser.cmd)
        {
            "ienter" ->
            {
                val ip = handle.socket.inetAddress.hostAddress
                val name = parser.getOption("n")

                println(ip)
                println(name)

                newMember(ip, name!!)
            }

            "iexit" ->
            {
                deleteMember(parser.getOption("n")!!)
            }

            "msg" ->
            {
                val msg = parser.getOption("m")
                val name = parser.getOption("n")

                newMessage(name + ": " + msg!!)
            }
        }
    }

    private fun onDestroy()
    {
        terminate()
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

    private fun newMember(ip: String, name: String)
    {
        memberList.add(Member(ip, name))

        updateNameListAndNotify()
    }

    private fun updateNameListAndNotify()
    {
        listData.clear()

        memberList.forEach {
            listData.add(it.name)
        }

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