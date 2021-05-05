package org.sopiro.chat.utils


class Parser(val str: String)
{
    lateinit var cmd: String
        private set

    lateinit var options: HashMap<String, String>
        private set

    lateinit var tokens: List<String>
        private set

    init
    {
        if (str.startsWith("|"))
        {
            parseData(str.substring(1, str.length - 1))
        } else
        {
            parseCommand(str)
        }
    }

    private fun parseData(str: String)
    {
        tokens = str.split("|")

        cmd = tokens[0]
    }

    private fun parseCommand(str: String)
    {
        tokens = str.split(" ")

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

            val starts = token.startsWith("\"")
            val ends = token.endsWith("\"")

            val starting = starts && !ends
            val mid = !starts && !ends
            val ending = !starts && ends
            val surrounded = starts && ends

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

    override fun toString(): String
    {
        return "cmd: $cmd, tokens: $tokens"
    }
}