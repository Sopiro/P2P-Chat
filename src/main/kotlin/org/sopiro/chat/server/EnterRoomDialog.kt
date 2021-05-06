package org.sopiro.chat.server

import org.sopiro.chat.utils.FontLib
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class EnterRoomDialog(
    window: JFrame,
    title: String,
    modal: Boolean,
    callBack: (name: String) -> Unit
) : JDialog(window, title, modal)
{
    init
    {
        val body = JPanel(GridLayout(1, 2, 10, 5))
        val foot = JPanel()

        val lblName = JLabel("닉네임")
        lblName.font = FontLib.font12
        val jtfName = JTextField(10)
        jtfName.font = FontLib.font12
        val okBtn = JButton("확인")
        okBtn.font = FontLib.font12

        jtfName.addActionListener {
            okBtn.doClick()
        }

        okBtn.addActionListener {
            dispose()
            callBack(jtfName.text)
        }

        body.add(lblName)
        body.add(jtfName)
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