package org.sopiro.chat.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopiro.chat.utils.Logger
import org.sopiro.chat.utils.Parser
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

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
        val parser = Parser(rawText)

        when (parser.cmd)
        {
            "start" ->
            {
                try
                {
                    val port = Integer.parseInt(parser.getOption("p"))
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
                logger.log("${server!!.numClients} Clients are existing")
            }

            "cls" ->
            {
                screen.text = ""
            }

            "noti" ->
            {
                server!!.notifyToAll(parser.getOption("m").toString());
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
