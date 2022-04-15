package com.example.pojo

data class User(val name:String ,val email:String ,val password:String ,val imagePath:String ,val job:String ,val country:String ,val gender:String)
{
    constructor():this("" ,"","","" ,"" ,"" ,"")
}
