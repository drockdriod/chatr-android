package ca.nanonorth.chatr.models

import com.stfalcon.chatkit.commons.models.IMessage

import com.stfalcon.chatkit.commons.models.IUser
import java.util.*


class Message(private var id: String = "",
        private var text: String = "",
        private var createdAt: Date = Date(), private var user: Author = Author()) : IMessage {



    override fun getId(): String {
        return id
    }

    fun setId(id:String) {
        this.id = id
    }

    override fun getText(): String {
        return text
    }

    fun setText(text:String){
        this.text = text
    }

    override fun getUser(): Author {
        return user
    }

    fun setUser(user: Author){
        this.user = user
    }

    override fun getCreatedAt(): Date {
        return createdAt
    }

    fun setCreatedAt(createdAt: Date){
        this.createdAt = createdAt
    }

}
