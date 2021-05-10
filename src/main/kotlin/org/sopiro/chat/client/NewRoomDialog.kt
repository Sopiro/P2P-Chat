package org.sopiro.chat.client

import org.sopiro.chat.utils.Resources
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*


class NewRoomDialog(
    window: JFrame,
    title: String,
    modal: Boolean,
    callBack: (name: String, roomName: String) -> Unit
) :
    JDialog(window, title, modal)
{
    init
    {
        this.setIconImage(Resources.icon)

        val body = JPanel(GridLayout(2, 2, 10, 5))
        val foot = JPanel()

        val lblName = JLabel(Resources.NICK_NAME)
        lblName.font = Resources.font12
        val lblRoomName = JLabel(Resources.ROOM_TITLE)
        lblRoomName.font = Resources.font12
        val jtfName = JTextField(10)
        jtfName.font = Resources.font12
        val jtfRoomName = JTextField(10)
        jtfRoomName.font = Resources.font12
        val okBtn = JButton(Resources.OK)
        okBtn.font = Resources.font12

        jtfRoomName.addActionListener {
            okBtn.doClick()
        }

        okBtn.addActionListener {
            dispose()
            callBack(jtfName.text, jtfRoomName.text)
        }

        body.add(lblName)
        body.add(jtfName)
        body.add(lblRoomName)
        body.add(jtfRoomName)
        foot.add(okBtn)

        add(body, BorderLayout.CENTER)
        add(foot, BorderLayout.SOUTH)

        (contentPane as JComponent).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        pack()
        setLocationRelativeTo(null)

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        isVisible = true
    }
}