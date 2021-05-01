package org.sopiro.chat

import java.time.LocalDateTime
import javax.swing.JTextArea

class Logger(val textArea: JTextArea)
{
    fun log(msg: String, alsoToConsole: Boolean = true)
    {
        val content = LocalDateTime.now().toString() + "|    " + msg + "\n"

        if (alsoToConsole) print(content)
        textArea.append(content)
    }
}