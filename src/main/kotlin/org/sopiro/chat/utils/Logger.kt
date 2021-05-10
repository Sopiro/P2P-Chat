package org.sopiro.chat.utils

import java.time.LocalDateTime
import javax.swing.JTextArea


class Logger(private val textArea: JTextArea?)
{
    fun log(msg: String, alsoToTextArea: Boolean = true)
    {
        val content = LocalDateTime.now().toString() + "|    " + msg + "\n"

        if (alsoToTextArea && textArea != null) textArea.append(content)
        print(content)
    }

    fun logNoTime(msg: String, alsoToTextArea: Boolean = true)
    {
        val content = "$msg\n"

        if (alsoToTextArea && textArea != null) textArea.append(content)
        print(content)
    }
}