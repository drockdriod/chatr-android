package ca.nanonorth.chatr.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import ca.nanonorth.chatr.models.ChatRoom
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.models.Message
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import org.json.JSONArray
import android.os.PersistableBundle
import android.support.v4.app.FragmentTransaction
import android.view.Menu
import android.view.MenuItem
import ca.nanonorth.chatr.models.Author
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.android.synthetic.main.content_appbar.*
import android.view.MenuInflater
import android.widget.ImageView
import android.widget.Toast
import ca.nanonorth.chatr.R.mipmap.logo
import ca.nanonorth.chatr.fragments.UserDialogFragment
import ca.nanonorth.chatr.holders.MessageSeparatorViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.utils.DateFormatter
import kotlinx.android.synthetic.main.content_profile.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.reflect.Type


class ChatRoomActivity : GlobalStateActivity(), UserDialogFragment.OnFragmentInteractionListener {
    private var chatRoom: ChatRoom? = null

    private var adapter: MessagesListAdapter<Message>? = null
    private val holdersConfig = MessageHolders()

    private val rootRef = FirebaseFirestore.getInstance()

    private var userDialog: UserDialogFragment? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)


        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        setSupportActionBar(toolbar)

//        toolbar.inflateMenu(R.menu.menu_chat_room)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.logo = null
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        val imageLoader = ImageLoader { imageView, url ->
            Picasso.with(this@ChatRoomActivity).load(url).into(imageView)
        }

        holdersConfig.setDateHeaderHolder(MessageSeparatorViewHolder::class.java)

        adapter = MessagesListAdapter(chatrManager!!.mAuth.currentUser!!.uid,holdersConfig, imageLoader)
        adapter!!.setDateHeadersFormatter({date ->
            when {
                DateFormatter.isToday(date) -> "Today"
                DateFormatter.isYesterday(date) -> "Yesterday"
                else -> DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
            }
        })
        messagesList.setAdapter(adapter)

        val extras = intent.extras

        val users = extras.getSerializable("users")

        println(users)

        chatRoom = ChatRoom(extras.getString("chatroom_id"), users as ArrayList<Author>)

        toolbar.title = chatRoom.toString()

        println("loaded chatroom")


        chatRoom!!.messageQuery()
            .addSnapshotListener { snapshots, _ ->

                if(snapshots != null){
                    val cachedMsgs = ArrayList<Message>()
                    for (dc in snapshots.documentChanges) {
                        when(dc.type) {
                            DocumentChange.Type.ADDED -> {

                                val message = this@ChatRoomActivity.chatRoom!!.constructMessage(dc.document)
                                if(snapshots.documentChanges.size == 1){
                                    this@ChatRoomActivity.adapter!!.addToStart(message, true)
                                }
                                else if(snapshots.metadata.isFromCache || snapshots.documentChanges.size > 1){
                                    println("cached...")
                                    cachedMsgs.add(message)
                                }
                                else{
                                    this@ChatRoomActivity.adapter!!.addToStart(message, true)

                                }
                            }
                            else -> {
                            }
                        }
                    }

                    if((snapshots.metadata.isFromCache || snapshots.documentChanges.size > 1) && cachedMsgs.size > 0){
                        adapter!!.addToEnd(cachedMsgs,false)
                    }

                }

            }




        input.setInputListener({userMsg ->
            //validate and send message
            doAsync {

                val userData = HashMap<String, Any>().apply {
                    put("ref", chatrManager?.mAuth!!.currentUser!!.uid)
                    put("first_name", chatrManager?.profile!!["first_name"].toString())
                    put("last_name", chatrManager?.profile!!["last_name"].toString())
                }

                chatRoom!!.sendMessage(userData, userMsg)
            }
            true
        })




    }


    override fun onUserListItemClick(user: Author) {
        this@ChatRoomActivity.userDialog!!.dismiss()

        doAsync {
            this@ChatRoomActivity.chatRoom!!.addUser(user)
            uiThread {
                val toast = Toast.makeText(this@ChatRoomActivity, "User added", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat_room, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        println("item $item")
        return when(item?.itemId){
            R.id.item_add_user -> {
                showAddUserDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    @SuppressLint("WrongConstant")
    private fun showAddUserDialog() {
        // Create an instance of the dialog fragment and show it
        println("show dialog")
        val revokedUsers : ArrayList<String> = ArrayList()
        chatRoom!!.users.forEach{user ->
            revokedUsers.add(user.id)
        }
        userDialog = UserDialogFragment.newInstance("Add a User", revokedUsers, true)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(android.R.id.content, userDialog)
                .addToBackStack(null).commit()
    }


    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {

        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onPause() {
        super.onPause()

//        adapter?.clear()
    }

}
