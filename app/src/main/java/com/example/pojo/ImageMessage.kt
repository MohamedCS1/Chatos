package com.example.pojo

import java.util.*

data class ImageMessage(val imagePath:String ,val SenderId:String ,val date:Date)
{
    constructor():this("" ,"" ,Date())
}
