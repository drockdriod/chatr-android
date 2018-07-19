package ca.nanonorth.chatr.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import ca.nanonorth.chatr.R

import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_appbar.*
import kotlinx.android.synthetic.main.fragment_profile.*

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val msg = intent.getStringExtra("hamal")

        hamalText.text = msg
    }

    

    fun goBack(view:View){
        finish()
    }

}
