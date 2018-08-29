package ca.nanonorth.chatr.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.Loader
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.content.Intent
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.widget.Toast
import ca.nanonorth.chatr.R
import ca.nanonorth.chatr.helpers.FirestoreDbHelper
import com.google.firebase.iid.FirebaseInstanceId

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_appbar.*
import org.jetbrains.anko.doAsync

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : GlobalStateActivity(), LoaderCallbacks<Cursor> {

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        if(chatrManager!!.mAuth.currentUser != null){
            chatrManager!!.authenticate()
            goToChatRooms()
        }

        // Set up the login form.
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
    }


    override fun onResume() {
        super.onResume()

        if(chatrManager!!.mAuth.currentUser != null){
            goToChatRooms()
        }
    }

    private fun goToChatRooms() {
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        if (chatrManager!!.mAuth.currentUser != null) {
            println(chatrManager!!.mAuth.currentUser?.email)

            chatrManager!!.authenticate()
            goToChatRooms()
            return
        }

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            // firebase login
            chatrManager!!.mAuth
                    .signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful) {
                            chatrManager!!.loadProfile()
                            chatrManager!!.authenticate()

                        } else {
                            // If sign in fails, display a message to the user.
                            println("signInWithEmail:failure ${task.exception}")

                            Toast.makeText(this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }

        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }


    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            cursor.moveToNext()
        }

    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    fun goToSignUp(view: View){
        val intent = Intent(this,SignUpActivity::class.java)

        startActivity(intent)
    }



}
