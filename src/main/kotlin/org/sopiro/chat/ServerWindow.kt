package org.sopiro.chat

import kotlinx.coroutines.*
import org.sopiro.chat.server.Server
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.math.log

class ServerWindow(title: String) : JFrame(title)
{
    private var body: JPanel
    private var foot: JPanel
    private var screen: JTextArea
    private var scroller: JScrollPane
    private var enterBtn: JButton
    private var cmdLine: JTextField

    private var logger: Logger
    private val defaultMsg = "start -p 1234"

    private var server: Server? = null

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
        logger = Logger(screen)

        screen.lineWrap = true
        screen.isEditable = false
        scroller = JScrollPane(screen, 20, 30)
        enterBtn = JButton("Enter")
        enterBtn.addActionListener {
            execute()
        }

        cmdLine = JTextField(80)
        cmdLine.addActionListener {
            enterBtn.doClick()
        }

        body.add(scroller, BorderLayout.CENTER)
        foot.add(cmdLine)
        foot.add(enterBtn)

        cmdLine.text = defaultMsg

        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        pack()

        setLocationRelativeTo(null)

        cmdLine.requestFocus()

        isVisible = true
    }

    private fun execute()
    {
        val rawText = cmdLine.text
        val command = Parser(rawText)

        when (command.cmd)
        {
            "start" ->
            {
                try
                {
                    val port = Integer.parseInt(command.getOption("p"))
                    tryStartServer(port)
                } catch (e: NumberFormatException)
                {
                    logger.log("Sets port with -p option correctly")
                }
            }

            "error" ->
            {
                logger.log("Error: $rawText")
            }

            "ls" ->
            {
                logger.log("${server!!.clients.size} Clients are existing")
            }

            "cls" ->
            {
                screen.text = ""
            }

            "exit" ->
            {
                server!!.terminate()
                dispose()
            }

            else -> logger.log("$rawText")
        }

        cmdLine.text = ""
    }

    private fun tryStartServer(port: Int)
    {
        if (server == null)
        {
            server = Server(port, logger)

            addWindowListener(object : WindowAdapter()
            {
                override fun windowClosing(e: WindowEvent)
                {
                    server!!.terminate()
                    super.windowClosing(e)
                }

            })

            CoroutineScope(Dispatchers.Default).launch {
                server!!.start()
            }
        } else
        {
            logger.log("Server is already online")
            return
        }
    }
}
