package ca.nanonorth.chatr.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Button

import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.adapters.UserListItemRecyclerAdapter
import ca.nanonorth.chatr.models.Author
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_user.view.*
import kotlinx.android.synthetic.main.result_list_item.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TITLE = "title"
private const val ARG_REVOKED_USERS = "revokedUsers"
private const val ARG_SHOW_BUTTON = "showButton"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddUserFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddUserFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class UserDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var dialogTitle: String? = null
    private var revokedUsers: ArrayList<String>? = null
    private var showButton: Boolean = true
    private var listener: OnFragmentInteractionListener? = null
    private var authorList: ArrayList<Author> = ArrayList()
    private var usersRecycler : RecyclerView? = null
    private var userListAdapter : UserListItemRecyclerAdapter? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogTitle = it.getString(ARG_TITLE)
            revokedUsers = it.getStringArrayList(ARG_REVOKED_USERS)
            showButton = it.getBoolean(ARG_SHOW_BUTTON)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_add_user, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_add_user, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.logo = null
        toolbar.title = dialogTitle
        toolbar.setNavigationOnClickListener {
            this.dismiss()
        }

        usersRecycler = view.findViewById(R.id.users_recycler) as RecyclerView
        usersRecycler!!.layoutManager = LinearLayoutManager(this.context)
        userListAdapter = UserListItemRecyclerAdapter(authorList, showButton = showButton, onClickListener = object : UserListItemRecyclerAdapter.UserListItemListener {
            override fun onPositionRowClick(v: View, user: Author) {
                onUserListItemClick(user)
            }
        })
        usersRecycler!!.adapter = userListAdapter

        FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("first_name")
                .orderBy("last_name")
                .limit(20)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        println("dataset: ${task.result.documents}")

                        task.result.documents.forEach {item ->
                            if(revokedUsers!!.indexOf(item.id) == -1){

                                val user = item.data
                                println("user item: $user")
                                val author = Author(id = item.id, name = "${user!!["first_name"]} ${user["last_name"]}", email = user["email"].toString())
                                authorList.add(author)

                            }

                        }

                        userListAdapter!!.notifyDataSetChanged()
                    }
                    else{
                        println("error: ${task.exception}")
                    }
                }


        return view
    }

    fun onUserListItemClick(user: Author) {
        listener?.onUserListItemClick(user)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onUserListItemClick(user: Author)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param title Dialog title
         * @param revokedUsers List of users revoked from
         * @param showButton Set button visibility
         * @return A new instance of fragment AddUserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(title: String, revokedUsers: ArrayList<String> = ArrayList(), showButton : Boolean = true) =
                UserDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_TITLE, title)
                        putStringArrayList(ARG_REVOKED_USERS, revokedUsers)
                        putBoolean(ARG_SHOW_BUTTON, showButton)
                    }
                }
    }
}
