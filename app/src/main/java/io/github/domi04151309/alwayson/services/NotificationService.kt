package io.github.domi04151309.alwayson.services

import android.app.Notification
import android.content.*
import android.graphics.drawable.Icon
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.objects.Global
import io.github.domi04151309.alwayson.objects.JSON
import org.json.JSONArray

class NotificationService : NotificationListenerService() {

    internal lateinit var localManager: LocalBroadcastManager
    private lateinit var prefs: SharedPreferences
    private var sentRecently: Boolean = false
    private var cache: Int = -1

    private val actionReceiver = object : BroadcastReceiver() {

        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Global.REQUEST_DETAILED_NOTIFICATIONS -> {
                    localManager.sendBroadcast(Intent(Global.DETAILED_NOTIFICATIONS).putExtra("notifications", activeNotifications))
                }
                Global.REQUEST_NOTIFICATIONS -> {
                    sendCount(true)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        localManager = LocalBroadcastManager.getInstance(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val filter = IntentFilter(Global.REQUEST_DETAILED_NOTIFICATIONS)
        filter.addAction(Global.REQUEST_NOTIFICATIONS)
        localManager.registerReceiver(actionReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        localManager.unregisterReceiver(actionReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        sendCount()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sendCount()
    }

    internal fun sendCount(force: Boolean = false) {
        if (!sentRecently) {
            sentRecently = true
            val notifications: Array<StatusBarNotification>
            val apps: ArrayList<String>
            var icons: ArrayList<Icon>
            var count = 0
            try {
                notifications = activeNotifications
                apps = ArrayList(notifications.size)
                icons = ArrayList(notifications.size)
                for (notification in notifications) {
                    if (
                            !notification.isOngoing
                            && !JSON.contains(JSONArray(prefs.getString("blocked_notifications", "[]")), notification.packageName)
                    ) {
                        if (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0) count++
                        if (!apps.contains(notification.packageName)) {
                            apps += notification.packageName
                            icons.add(notification.notification.smallIcon)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(Global.LOG_TAG, e.toString())
                count = 0
                icons = arrayListOf()
            }
            if (cache != count || force) {
                cache = count
                localManager.sendBroadcast(Intent(Global.NOTIFICATIONS).putExtra("count", count).putExtra("icons", icons))
            }
            Handler().postDelayed({ sentRecently = false }, 100)
        }
    }
}