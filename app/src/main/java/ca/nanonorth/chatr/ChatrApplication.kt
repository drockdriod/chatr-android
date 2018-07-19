package ca.nanonorth.chatr

import android.app.Application
import android.content.SharedPreferences
import ca.nanonorth.chatr.managers.ChatrManager
import com.google.firebase.messaging.FirebaseMessaging
import android.content.ComponentName
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.content.Context.ACTIVITY_SERVICE
import com.google.firebase.auth.FirebaseAuth


class ChatrApplication : Application() {
    var chatrManager : ChatrManager? = null
    override fun onCreate() {
        super.onCreate()
        chatrManager = ChatrManager()

    }

    fun isAppInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }

        return isInBackground
    }
}