package org.sopiro.chat.utils

import java.awt.Font
import java.awt.Image
import javax.imageio.ImageIO

object Resources
{
    val font16 = Font("sansserif", Font.PLAIN, 16)
    val font12 = Font("sansserif", Font.PLAIN, 12)
    val icon: Image

    var language: Lang = Lang.ENG

    enum class Lang
    {
        KOR, ENG
    }

    val COLUMN_NAMES: List<String>
        get()
        {
            return if (language == Lang.KOR)
                listOf("방장", "방제", "인원수")
            else
                listOf("Master", "Title", "Members")
        }


    val OK: String
        get()
        {
            return if (language == Lang.KOR)
                "확인"
            else
                "OK"
        }

    val SETTINGS: String
        get()
        {
            return if (language == Lang.KOR)
                "설정"
            else
                "Settings"
        }

    val MASTER_SERVER_CLOSED: String
        get()
        {
            return if (language == Lang.KOR)
                "마스터 서버가 닫혀있습니다."
            else
                "Master server is currently closed"
        }


    val MASTER_SERVER_JUST_CLOSED: String
        get()
        {
            return if (language == Lang.KOR)
                "마스터 서버가 닫혔습니다."
            else
                "Master server closed"
        }

    val MASTER_SERVER_SETTING: String
        get()
        {
            return if (language == Lang.KOR)
                "마스터 서버 설정"
            else
                "Master server setting"
        }

    val NEW_ROOM: String
        get()
        {
            return if (language == Lang.KOR)
                "방 만들기"
            else
                "Create"
        }

    val ENTER_ROOM: String
        get()
        {
            return if (language == Lang.KOR)
                "접속"
            else
                "Enter"
        }

    val REFRESH: String
        get()
        {
            return if (language == Lang.KOR)
                "새로고침"
            else
                "Refresh"
        }

    val CORRECT_PLS: String
        get()
        {
            return if (language == Lang.KOR)
                "제대로 입력해주세요"
            else
                "Please enter correctly"
        }

    val CORRECT_PLS_IP_PORT: String
        get()
        {
            return if (language == Lang.KOR)
                "서버ip와 port를 제대로 입력해주세요."
            else
                "Please enter the server IP, port correctly"
        }

    val SELECT_PLS: String
        get()
        {
            return if (language == Lang.KOR)
                "방을 선택해 주세요"
            else
                "Please select a room"
        }

    val HOST_ERR: String
        get()
        {
            return if (language == Lang.KOR)
                "당신은 방을 만들수 없습니다.\n호스트 서버 에러"
            else
                "You can't host a room.\nNetwork error"
        }

    val NOTICE: String
        get()
        {
            return if (language == Lang.KOR)
                "알림"
            else
                "Notice"
        }

    val LANG_SETT: String
        get()
        {
            return if (language == Lang.KOR)
                "언어 설정"
            else
                "Language setting"
        }

    val NICK_NAME: String
        get()
        {
            return if (language == Lang.KOR)
                "닉네임"
            else
                "Nickname"
        }

    val ROOM_TITLE: String
        get()
        {
            return if (language == Lang.KOR)
                "방 이름"
            else
                "Room title"
        }

    val MS_IP: String
        get()
        {
            return if (language == Lang.KOR)
                "마스터서버 IP"
            else
                "Master server IP"
        }

    val MS_PORT: String
        get()
        {
            return if (language == Lang.KOR)
                "마스터서버 port"
            else
                "Master server port"
        }

    val ENTER: String
        get()
        {
            return if (language == Lang.KOR)
                "입력"
            else
                "Enter"
        }

    val MEMBERS: String
        get()
        {
            return if (language == Lang.KOR)
                "참가자"
            else
                "Members"
        }

    val LEFT_ROOM: String
        get()
        {
            return if (language == Lang.KOR)
                "방장이 방을 나갔습니다."
            else
                "Room host has left the room"
        }

    val SOMEONE_ENTER: String
        get()
        {
            return if (language == Lang.KOR)
                "님이 입장하셨습니다."
            else
                "entered the room."
        }

    val SOMEONE_OUT: String
        get()
        {
            return if (language == Lang.KOR)
                "님이 퇴장하셨습니다."
            else
                "left the room."
        }

    val ROOM_LIST: String
        get()
        {
            return if (language == Lang.KOR)
                "방 목록"
            else
                "Room List"
        }


    init
    {
        icon = ImageIO.read(Resources.javaClass.classLoader.getResourceAsStream("icon.png"))
    }
}