package org.sopiro.chat.client

import org.sopiro.chat.utils.Resources
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
        this.setIconImage(Resources.icon)

        val body = JPanel(GridLayout(2, 2, 10, 5))
        val foot = JPanel()

        val lblIp = JLabel(Resources.MS_IP)
        lblIp.font = Resources.font12
        val lblPort = JLabel(Resources.MS_PORT)
        lblPort.font = Resources.font12
        val jtfIp = JTextField(10)
        jtfIp.text = serverIP
        jtfIp.font = Resources.font12
        val jtfPort = JTextField(10)
        jtfPort.text = serverPort.toString()
        jtfPort.font = Resources.font12
        val okBtn = JButton(Resources.OK)
        okBtn.font = Resources.font12

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

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        isVisible = true
    }
}