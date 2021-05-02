package org.sopiro.chat

import org.sopiro.chat.client.Client
import org.sopiro.chat.client.ClientWindow

fun main()
{
    val client = Client("127.0.0.1", 1234)
//    val client = Client("222.101.80.229", 1234)

    while (!client.readyToGo)
    {
        Thread.sleep(10)
    }

    val window = ClientWindow("Client", client)
}