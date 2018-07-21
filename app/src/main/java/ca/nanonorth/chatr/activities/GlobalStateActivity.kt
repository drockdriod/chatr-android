package ca.nanonorth.chatr.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ca.nanonorth.chatr.ChatrApplication
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.managers.ChatrManager
import kotlinx.android.synthetic.main.content_appbar.*

open class GlobalStateActivity : AppCompatActivity() {

    var chatrManager : ChatrManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        chatrManager = (applicationContext as ChatrApplication).chatrManager


        setSupportActionBar(toolbar)
    }

    override fun onStart() {
        super.onStart()


    }


}