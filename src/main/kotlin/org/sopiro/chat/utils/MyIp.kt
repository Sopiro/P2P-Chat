package org.sopiro.chat.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


object MyIp
{
    fun ip(): String
    {
        val myip = URL("https://api.myip.com")
        val reader = BufferedReader(InputStreamReader(myip.openStream()))

        var res = ""
        var line: String?

        while ((reader.readLine().also { line = it }) != null)
        {
            res += line + "\n"
        }

        return res.split("\"")[3]
    }
}
