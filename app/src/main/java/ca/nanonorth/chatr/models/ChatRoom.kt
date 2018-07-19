package ca.nanonorth.chatr.models

import android.support.design.widget.Snackbar
import ca.nanonorth.chatr.managers.Constants
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatRoom(val ref:String, val users: ArrayList<Author> = ArrayList()) {

    var messages: ArrayList<Message>? = null

    private val rootRef = FirebaseFirestore.getInstance()
    private val chatroomDocRef = rootRef
            .collection("chatrooms")
            .document(ref)
    private val messagesRef = rootRef
            .collection("messages")
            .whereEqualTo("chatroom_id", ref)


    private var messageRootRef = rootRef.collection("messages")


    override fun toString(): String {
        val u = ArrayList<String>()
        users.forEach{user ->
            u.add(user.name)
        }
        return u.joinToString(", ")
    }

    fun constructMessage(item : DocumentSnapshot ) : Message {
        val user = JSONObject(item.data!!.get("user").toString())
        val author = Author(name = user["first_name"].toString() + " " + user["last_name"].toString(),
                id = user["ref"].toString())

        println(user)

        val message = Message(
                text = item.data!!.get("text").toString(),
                createdAt = item.data!!.get("date_created") as Date,
                user = author)

        return message
    }

    fun messageQuery(): Query {

        return messagesRef
                .orderBy("date_created", Query.Direction.DESCENDING)
                .limit(20)


    }

    fun getTitle(currentUser : String = "") : String{
        val u = ArrayList<String>()
        println(u)
        users.forEach{user ->
            if(user.id != currentUser){
                u.add(user.name)
            }
        }
        return u.joinToString(", ")
    }

    fun addUser(user: Author) : String?{
        val userSubMap: MutableMap<String, Any> = HashMap()
        userSubMap.put(user.id, user.exportAsMap())

        val userMap : MutableMap<String, HashMap<String, Any>> = HashMap()
        userMap.put("users", userSubMap as HashMap<String, Any>)

        val response = chatroomDocRef
                .set(userMap as Map<String, Any>, SetOptions.merge())
                .addOnCompleteListener { task1 ->
                    return@addOnCompleteListener

                    }

        if (!response.isSuccessful) {
            return response.exception!!.message
        } else {

            val response2 = addChatroomRefToUser(user)

            return if(response2.isSuccessful){
                this.users.add(user)

                "Success"
            } else{
                response.exception!!.message
            }
        }



    }

    fun addChatroomRefToUser(user : Author): Task<Void> {
        val chatRoomIdObj : MutableMap<String, Boolean> = HashMap()
        chatRoomIdObj.put(this.ref, true)

        val chatRoomMap : MutableMap<String, Any> = HashMap()
        chatRoomMap.put("chatrooms", chatRoomIdObj)
        return rootRef
                .collection("users")
                .document(user.id)
                .set(chatRoomMap, SetOptions.merge())
                .addOnCompleteListener{ task2 ->
                    return@addOnCompleteListener
                }
    }

    fun sendMessage(userData : HashMap<String, Any>, userMsg : CharSequence){


        val user = Author(name = "${userData["first_name"]} ${userData["last_name"]}", id = userData["ref"].toString())
        val message = Message(user = user, text = userMsg.toString(), createdAt = Date())

        val data = HashMap<String, Any>()
        data["chatroom_id"] = this.ref
        data["date_created"] = message.createdAt
        data["text"] = message.text
        data["user"] = userData

        val dataJson: JSONObject = JSONObject().apply {
            this@apply.put("sender_id", user.id)
            this@apply.put("sender_name", user.name)
            this@apply.put("message", userMsg.toString())
        }

        messageRootRef.add(data)

        doAsync {
            val request = "${Constants.API_URL}/chatrooms/${this@ChatRoom.ref}/notify"
                    .httpPost()
                    .body(dataJson.toString())

            request.headers["Content-Type"] = "application/json"
            request.responseJson { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        println("success")
                    }
                }
            }
        }

    }

}