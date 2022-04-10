package com.example.pojo

import java.util.*

interface Message {
    val senderId:String
    val receiverId:String
    val date:Date
    val type:String
}