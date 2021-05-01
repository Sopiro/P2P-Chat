package org.sopiro.chat

import org.sopiro.chat.parser.Parser
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.time.LocalDateTime
import javax.swing.*

class ServerWindow(title: String) : JFrame(title)
{
    private var body: JPanel
    private var foot: JPanel
    private var screen: JTextArea
    private var scroller: JScrollPane
    private var enterBtn: JButton
    private var cmdLine: JTextField

    private val defaultMsg = "start -p 1234"

    init
    {
        // JFrame settings
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE

        (contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Define controls
        body = JPanel()
        body.layout = BorderLayout()

        foot = JPanel()
        foot.layout = FlowLayout(FlowLayout.RIGHT)

        screen = JTextArea(20, 80)
        screen.lineWrap = true
        screen.isEditable = false
        scroller = JScrollPane(screen, 20, 30)
        enterBtn = JButton("Enter")
        cmdLine = JTextField(80)

        cmdLine.addActionListener {
            when (it.id)
            {
                1001 -> enterBtn.doClick()
            }
        }

        body.add(scroller, BorderLayout.CENTER)
        foot.add(cmdLine)
        foot.add(enterBtn)

        cmdLine.text = defaultMsg
        enterBtn.addActionListener {

            val command = Parser(cmdLine.text)

            when (command.cmd)
            {
                "start" ->
                {
                    try
                    {
                        val port = Integer.parseInt(command.getOption("p"))
                        start(port)
                    } catch (e: NumberFormatException)
                    {
                        start(1234)
                    }
                }

                "error" -> println("error")

                else -> println(cmdLine.text)
            }

            cmdLine.text = ""
        }

        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        pack()

        setLocationRelativeTo(null)

        cmdLine.requestFocus()

        isVisible = true
    }

    private fun appendMessage(msg: String)
    {
        val currentDate = LocalDateTime.now()

        screen.append("$currentDate|    ")
        screen.append(msg + "\n")
    }

    fun start(port: Int)
    {
        appendMessage("Server started on port: $port")
    }
}
