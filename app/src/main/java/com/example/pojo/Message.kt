package com.example.pojo

import java.util.*

data class Message(val message:String, val senderId:String, val date: Date?)
{
    constructor():this("" ,"" ,Date())
}
