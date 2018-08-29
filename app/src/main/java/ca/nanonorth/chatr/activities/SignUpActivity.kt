package ca.nanonorth.chatr.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.R.id.link_login

import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : GlobalStateActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_AuthStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        if(chatrManager!!.mAuth.currentUser != null){
            chatrManager!!.authenticate()
            goToChatRooms()
        }

    }

    private fun goToChatRooms() {
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    fun goToLogin(view: View){
        val intent = Intent(this, LoginActivity::class.java)


        this.startActivity(intent)
    }

}
