package com.example.pojo

import java.util.*

data class TextMessage(
    val message: String,
    override val senderId: String,
    override val receiverId: String,
    override val senderName: String,
    override val receiverName: String,
    override val date: Date,
    override val type: String = MessageType.TEXT
) : Message {
    constructor() : this("", "", "", "", "", Date(), "")
}
