package org.sopiro.chat.parser


class Parser(str: String)
{
    var cmd: String
    val options: Map<String, String>

    init
    {
        val tokens = str.split(" ")

        cmd = tokens[0]
        options = HashMap()

        var count = 0
        str.toCharArray().forEach {
            if (it == '"')
                count++
        }

        if (count % 2 != 0) cmd = "error"

        var option = ""
        var content = ""
        var inside = false

        for (i in 1 until tokens.size)
        {
            val token = tokens[i]

            if (token.startsWith("-"))
            {
                option = token.substring(1, token.length)
                continue
            }

            val starting = token.startsWith("\"") && !token.endsWith("\"")
            val mid = !token.startsWith("\"") && !token.endsWith("\"")
            val ending = !token.startsWith("\"") && token.endsWith("\"")
            val surrounded = token.startsWith("\"") && token.endsWith("\"")

            if ((option != "" && content == "") || (option != "" && inside))
            {
                if (inside)
                {
                    if (mid)
                    {
                        content += " $token"
                    } else if (ending)
                    {
                        options[option] = content + " " + token.substring(0, token.length - 1)
                        option = ""
                        content = ""
                        inside = false
                    }
                } else
                {
                    if (surrounded)
                    {
                        options[option] = token.substring(1, token.length - 1)
                        option = ""
                    } else if (!inside && mid)
                    {
                        options[option] = token
                        option = ""
                    } else if (!inside && starting)
                    {
                        inside = true
                        content = token.substring(1, token.length)
                    }
                }
            }
        }
    }

    fun getOption(option: String): String?
    {
        return options[option]
    }
}