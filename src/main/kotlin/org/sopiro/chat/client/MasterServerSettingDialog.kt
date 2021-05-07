package org.sopiro.chat.client

import org.sopiro.chat.utils.FontLib
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class MasterServerSettingDialog(
    window: JFrame,
    title: String,
    modal: Boolean,
    serverIP: String,
    serverPort: Int,
    callBack: (ip: String, port: String) -> Unit
) :
    JDialog(window, title, modal)
{
    init
    {
        val body = JPanel(GridLayout(2, 2, 10, 5))
        val foot = JPanel()

        val lblIp = JLabel("마스터서버 IP")
        lblIp.font = FontLib.font12
        val lblPort = JLabel("마스터서버 port")
        lblPort.font = FontLib.font12
        val jtfIp = JTextField(10)
        jtfIp.text = serverIP
        jtfIp.font = FontLib.font12
        val jtfPort = JTextField(10)
        jtfPort.text = serverPort.toString()
        jtfPort.font = FontLib.font12
        val okBtn = JButton("확인")
        okBtn.font = FontLib.font12

        jtfPort.addActionListener {
            okBtn.doClick()
        }

        okBtn.addActionListener {
            dispose()
            callBack(jtfIp.text, jtfPort.text)
        }

        body.add(lblIp)
        body.add(jtfIp)
        body.add(lblPort)
        body.add(jtfPort)
        foot.add(okBtn)

        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        (contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        pack()
        setLocationRelativeTo(null)

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE;

        isVisible = true
    }
}