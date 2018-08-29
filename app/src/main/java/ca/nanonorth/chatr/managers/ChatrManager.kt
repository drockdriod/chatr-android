package ca.nanonorth.chatr.managers

import android.os.CountDownTimer
import ca.nanonorth.chatr.helpers.FirestoreDbHelper
import ca.nanonorth.chatr.models.Author
import ca.nanonorth.chatr.models.ChatRoom
import ca.nanonorth.chatr.models.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.doAsync

class ChatrManager {
    private var chatRoomMessagesCache: HashMap<String, List<Message>>? = HashMap()
    var profile : HashMap<String, Any> = HashMap()
    var author: Author? = null

    val mAuth = FirebaseAuth.getInstance()
    init {
        if(mAuth.currentUser != null){
            loadProfile()
        }
    }

    fun loadProfile(){
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(mAuth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        profile = task.result.data as HashMap<String, Any>
                        val name: String = "${profile["first_name"]} ${profile["last_name"]}"
                        author = Author(mAuth.currentUser!!.uid,name, profile["email"].toString())
                    }
                }
    }

    fun initChatroom(usersMap: MutableMap<String, Author>): Task<DocumentReference> {
        val moddedMap : MutableMap<String, Any> = HashMap()
        usersMap.keys.forEach {key ->
            moddedMap.put(key, usersMap[key]!!.exportAsMap())

        }

        val chatRoomMap : MutableMap<String, Any> = HashMap()

        chatRoomMap["users"] = moddedMap
        return FirebaseFirestore.getInstance().collection("chatrooms")
                .add(chatRoomMap)
    }

    fun authenticate(){
        doAsync {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {

                val authTokenMap : MutableMap<String, Any> = HashMap(1)
                authTokenMap.put("auth_token",it.result.token)
                FirestoreDbHelper().updateUser(this@ChatrManager.mAuth.currentUser!!.uid,authTokenMap)
            }
        }

    }


}