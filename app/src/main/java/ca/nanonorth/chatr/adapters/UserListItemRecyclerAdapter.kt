package ca.nanonorth.chatr.adapters

import android.opengl.Visibility
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.R.drawable.ic_baseline_supervised_user_circle_24px
import ca.nanonorth.chatr.models.Author
import kotlinx.android.synthetic.main.result_list_item.view.*

class UserListItemRecyclerAdapter(private val dataSet: ArrayList<Author>, private var showButton : Boolean = true, private val onClickListener: UserListItemListener)
    : RecyclerView.Adapter<UserListItemRecyclerAdapter.ViewHolder>() {


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val linearView: LinearLayout) : RecyclerView.ViewHolder(linearView)
//    {
//        private var params = linearView.layoutParams!!
//        private var layoutButton : Button? = linearView.findViewById(R.id.add_item)
//
//        private fun hideButton() {
//            params.height = 0
//            layoutButton!!.layoutParams = params   //Or This one.
//
//        }
//    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): UserListItemRecyclerAdapter.ViewHolder {
        // create a new view
        val linearView = LayoutInflater.from(parent.context)
                .inflate(
                        R.layout.result_list_item,
                        parent,
                        false
                ) as LinearLayout

        return ViewHolder(linearView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.linearView.result_title.text = dataSet[position].name
        holder.linearView.result_sub_title.text = dataSet[position].getEmail()
        holder.linearView.result_icon.setImageResource(ic_baseline_supervised_user_circle_24px)

        if(showButton){

            holder.itemView.findViewById<Button>(R.id.add_item).setOnClickListener{
                onClickListener.onPositionRowClick(it,dataSet[position])
            }
        }
        else{
            holder.itemView.findViewById<Button>(R.id.add_item).visibility = View.INVISIBLE
            holder.itemView.setOnClickListener {
                onClickListener.onPositionRowClick(it,dataSet[position])
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface UserListItemListener {

        fun onPositionRowClick(v: View, user: Author)
    }
}
