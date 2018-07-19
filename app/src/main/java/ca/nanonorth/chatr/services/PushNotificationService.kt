package ca.nanonorth.chatr.services

import android.support.constraint.Constraints.TAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import ca.nanonorth.chatr.ChatrApplication


class PushNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage!!.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

        }

        println("notificaiton ${remoteMessage.notification}")

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
            handleNotification(remoteMessage.notification!!.body!!)
        }

    }

    private fun handleNotification(message: String) {
        if (!(applicationContext as ChatrApplication).isAppInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent("pushNotification")
            pushNotification.putExtra("message", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: " + token!!)
        println("nana $token")
    }
}