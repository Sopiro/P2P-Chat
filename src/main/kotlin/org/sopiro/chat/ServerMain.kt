package org.sopiro.chat

import org.sopiro.chat.server.ServerWindow
import org.sopiro.chat.utils.FontLib
import javax.swing.UIManager

fun main()
{
    UIManager.put("OptionPane.messageFont", FontLib.font12)
    UIManager.put("OptionPane.buttonFont", FontLib.font12)
    val window = ServerWindow("마스터 서버")
//    window.start()
}