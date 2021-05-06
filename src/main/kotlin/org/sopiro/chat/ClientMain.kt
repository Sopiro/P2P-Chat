package org.sopiro.chat

import org.sopiro.chat.client.ClientWindow
import org.sopiro.chat.utils.FontLib
import javax.swing.UIManager

fun main()
{
    UIManager.put("OptionPane.messageFont", FontLib.font12)
    UIManager.put("OptionPane.buttonFont", FontLib.font12)
    val window = ClientWindow("방 목록")
}