package ca.nanonorth.chatr.holders

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders
import java.util.*
import android.widget.TextView
import ca.nanonorth.chatr.R


class MessageSeparatorViewHolder(itemView: View?) : MessageHolders.DefaultDateHeaderViewHolder(itemView) {
    init {
        this.text = itemView!!.findViewById(R.id.messageText) as TextView
    }

    override fun onBind(date: Date?) {
        super.onBind(date)
    }

    fun addStringHeader(header: String) {
        if(text != null){
            text.text = header
        }
    }
}