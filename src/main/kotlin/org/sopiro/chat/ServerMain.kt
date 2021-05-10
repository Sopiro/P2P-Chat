package org.sopiro.chat

import org.sopiro.chat.server.ServerWindow
import org.sopiro.chat.utils.Resources
import javax.swing.UIManager


fun main()
{
    UIManager.put("OptionPane.messageFont", Resources.font12)
    UIManager.put("OptionPane.buttonFont", Resources.font12)
    ServerWindow("Master Server")
}