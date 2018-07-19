package ca.nanonorth.chatr.models

import com.stfalcon.chatkit.commons.models.IUser
import android.R.attr.name
import java.io.Serializable


class Author(private var id: String = "", private var name: String = "", private var email: String = "", private var avatar: String = "") : IUser, Serializable {
    /*...*/

    fun getEmail() : String {
        return this.email
    }

    fun setEmail(email: String) {
        this.email = email
    }

    override fun getId(): String {
        return this.id
    }

    fun setId(id:String){
        this.id = id
    }

    override fun getName(): String {
        return this.name
    }

    fun setName(name:String){
        this.name = name
    }

    override fun getAvatar(): String {
        return this.avatar
    }

    fun setAvatar(avatar:String){
        this.avatar = avatar
    }

    fun exportAsMap(): Map<String, Any>{
        val userMap : MutableMap<String,Any> = HashMap(4)
        val userName: List<String> = this.name.split(" ")
        userMap.put("first_name", userName.get(0))
        userMap.put("last_name", userName.get(1))
        userMap.put("ref", this.id)
        userMap.put("email", this.email)

        return userMap
    }

}