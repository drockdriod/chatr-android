package ca.nanonorth.chatr.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.View
import android.widget.SimpleAdapter
import ca.nanonorth.chatr.models.ChatRoom
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.fragments.UserDialogFragment
import ca.nanonorth.chatr.managers.Constants
import ca.nanonorth.chatr.models.Author
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_appbar.*
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.android.synthetic.main.nav_header.view.*
import android.support.design.widget.NavigationView


class MainActivity : GlobalStateActivity(), UserDialogFragment.OnFragmentInteractionListener {

    private var listChatRooms: ArrayList<ChatRoom> = ArrayList()
    private var chatRoomTiles: ArrayList<Map<String, String>> = ArrayList()
    private var adapter: SimpleAdapter? = null

    private var userDialog: UserDialogFragment? = null

    private lateinit var mDrawerLayout: DrawerLayout
    private var navigation: NavigationView? = null


    override fun onUserListItemClick(user: Author) {

        dismissAllDialogs(userDialog!!.fragmentManager!!)

        val usersMap : MutableMap<String, Author> = HashMap()
        usersMap.put(chatrManager!!.author!!.id, chatrManager!!.author!!)
        usersMap.put(user.id, user)

        val usersList : ArrayList<Author> = ArrayList()
        usersList.add(user)
        usersList.add(chatrManager!!.author!!)


        val response = chatrManager!!.initChatroom(usersMap)
        response.addOnCompleteListener {

            val chatRoom = ChatRoom(response.result.id, usersList)

            listChatRooms.add(chatRoom)

            val datum = HashMap<String, String>(2)
            datum["title"] = chatRoom.getTitle(chatrManager!!.mAuth.currentUser!!.uid)
            datum["date"] = ""
            this@MainActivity.chatRoomTiles.add(datum)
            this@MainActivity.adapter?.notifyDataSetChanged()

            val response1 = chatRoom.addChatroomRefToUser(chatrManager!!.author!!)

            if(response1.isSuccessful){
                println("success user added")
            }
            else{
                println("error" + response.exception?.message)
            }
            val response2 = chatRoom.addChatroomRefToUser(user)

            if(response2.isSuccessful){
                println("success user added")
            }
            else{
                println("error" + response.exception?.message)
            }

            this@MainActivity.goToChatRoom(chatRoom)
        }




    }

    private fun dismissAllDialogs(manager: FragmentManager) {
        val fragments = manager.getFragments() ?: return

        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                fragment.dismissAllowingStateLoss()
            }

            val childFragmentManager = fragment.childFragmentManager
            dismissAllDialogs(childFragmentManager!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(chatrManager!!.mAuth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        adapter = SimpleAdapter(this, chatRoomTiles,
                android.R.layout.simple_list_item_2,
                arrayOf("title", "date"),
                intArrayOf(android.R.id.text1, android.R.id.text2))
        list_chatrooms.adapter = adapter

//        GetChatroomsTask(this).execute(chatrManager!!.mAuth.currentUser!!.uid)
        getChatRooms()



        setSupportActionBar(toolbar)

        initNavigation()


        list_chatrooms.setOnItemClickListener({ _, _, position, id ->
            goToChatRoom(listChatRooms[position])
        })

        fab_add_chatroom.setOnClickListener {
            showAddUserDialog()
        }

    }

    private fun initNavigation(){
        mDrawerLayout = findViewById(R.id.drawer_layout)

        val logoView = toolbar.getChildAt(0)
        logoView.setOnClickListener {
            mDrawerLayout.openDrawer(GravityCompat.START)
            mDrawerLayout.nav_drawer_title.text = chatrManager!!.author!!.name
        }

        navigation = findViewById<View>(R.id.nav_view) as NavigationView

        navigation!!.setNavigationItemSelectedListener {
            return@setNavigationItemSelectedListener when(it.itemId){
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SignUpActivity::class.java).apply {
                        putExtra("email",  chatrManager!!.author!!.getEmail())
                    }

                    this.startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun goToChatRoom(chatRoom: ChatRoom){
        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            val extras = Bundle()
            extras.putString("chatroom_id",  chatRoom.ref)
            extras.putParcelableArrayList("users", chatRoom.users as java.util.ArrayList<out Parcelable>)
            putExtras(extras)
        }


        this.startActivity(intent)
    }


    @SuppressLint("WrongConstant")
    private fun showAddUserDialog() {
        // Create an instance of the dialog fragment and show it
        println("show dialog")
        val revokedUsers : ArrayList<String> = ArrayList()
        revokedUsers.add(chatrManager!!.mAuth.currentUser!!.uid)

        userDialog = UserDialogFragment.newInstance("Choose a User", revokedUsers, false)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(android.R.id.content, userDialog)
                .addToBackStack(null).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory( Intent.CATEGORY_HOME )
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }


    private fun convertToAuthorList(users : JSONArray) : ArrayList<Author>{
        val userList : ArrayList<Author> = ArrayList()
        for(i in 0 until users.length()) {

            userList.add(Author(users.getJSONObject(i)["ref"] as String,"${users.getJSONObject(i)["first_name"]} ${users.getJSONObject(i)["last_name"]}", users.getJSONObject(i)["email"]?.toString() ?: "" ))
        }

        return userList
    }

    private fun getChatRooms() {
        println("uid: ${chatrManager!!.mAuth.currentUser!!.uid}")
        doAsync {
            "${Constants.API_URL}/chatrooms/${chatrManager!!.mAuth.currentUser!!.uid}".httpGet()
                    .responseJson { _, _, result ->
                        //do something with response
                        when (result) {
                            is Result.Failure -> {
                                val ex = result.getException()
                                println(ex)
                            }
                            is Result.Success -> {
                                val chatrooms: JSONArray = result.get().array()
                                println("test 1")
                                for (i in 0 until chatrooms.length()) {
                                    val chatRoom = ChatRoom(chatrooms.getJSONObject(i)["ref"].toString(), convertToAuthorList(chatrooms.getJSONObject(i)["users"] as JSONArray) )
                                    this@MainActivity.listChatRooms.add(chatRoom)

                                    println("chatroom id: ${chatrooms.getJSONObject(i)["ref"]}")
                                    FirebaseMessaging.getInstance().subscribeToTopic(chatrooms.getJSONObject(i)["ref"].toString())

                                    val datum = HashMap<String, String>(2)
                                    datum["title"] = this@MainActivity.listChatRooms[i].getTitle(chatrManager!!.mAuth.currentUser!!.uid)
                                    datum["date"] = ""

                                    if (chatrooms.getJSONObject(i).has("message")) {
                                        val recentMessage = JSONObject(chatrooms.getJSONObject(i)["message"].toString())
                                        datum["date"] = recentMessage["time_ago"].toString()
                                        if (recentMessage.has("text")) {
                                            var length = 20
                                            if (recentMessage["text"].toString().length < 20) {
                                                length = recentMessage["text"].toString().length
                                            }
                                            datum["title"] = datum["title"] + " - " + recentMessage["text"].toString().substring(0, length) + "..."
                                        }
                                    }
                                    this@MainActivity.chatRoomTiles.add(datum)
                                }

                                this@MainActivity.adapter?.notifyDataSetChanged()
                                println("test 2")


                            }
                        }
                    }
        }


    }

}




