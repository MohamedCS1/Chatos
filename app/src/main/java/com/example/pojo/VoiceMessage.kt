package com.example.pojo

import java.util.*

data class VoiceMessage(
    val voiceUID:String,
    override val senderId: String,
    override val receiverId: String,
    override val senderName: String,
    override val receiverName: String,
    override val date: Date,
    override val type: String = MessageType.VOICE
):Message {
    constructor() : this("", "","","", "", Date(), "")
}
