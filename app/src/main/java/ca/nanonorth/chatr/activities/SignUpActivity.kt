package ca.nanonorth.chatr.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import ca.nanonorth.chatr.R

import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : GlobalStateActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_AuthStyle)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)



    }

}
