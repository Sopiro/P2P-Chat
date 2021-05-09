package org.sopiro.chat.utils

import java.awt.Font
import java.awt.Image
import javax.imageio.ImageIO

object Resources
{
    val font16 = Font("sansserif", Font.PLAIN, 16)
    val font12 = Font("sansserif", Font.PLAIN, 12)
    val icon: Image

    init
    {
        icon = ImageIO.read(Resources.javaClass.classLoader.getResourceAsStream("icon.png"))
    }
}