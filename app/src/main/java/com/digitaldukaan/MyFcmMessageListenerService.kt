package com.digitaldukaan

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.clevertap.android.sdk.CleverTapAPI
import com.digitaldukaan.constants.AFInAppEventParameterName
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFcmMessageListenerService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: ${message.data}")
        message.data.apply {
            try {
                if (size > 0) {
                    val extras = Bundle()
                    for ((key, value) in this) {
                        extras.putString(key, value)
                    }
                    val info = CleverTapAPI.getNotificationInfo(extras)
                    if (info.fromCleverTap) {
                        Log.d(TAG, "CLEVER-TAP NOTIFICATIONS :: $info")
                        //CleverTapAPI.createNotification(applicationContext, extras)
                        prepareNotification(extras.getString("nt"), extras.getString("nm"), extras.getString("wzrk_dl"))
                    }
                }
            } catch (t: Throwable) {
                Log.d(TAG, "Error parsing FCM message", t)
            }
        }
        if (message.notification != null) {
            Log.d(TAG, "FIREBASE NOTIFICATIONS")
            val title = message.notification?.title
            val body = message.notification?.body
            prepareNotification(title, body)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, "onNewToken: $p0")
    }

    private fun prepareNotification(title: String?, message: String?, deepLinkUrl: String? = "") {
        val intent = Intent(this, MainActivity::class.java)
        if (deepLinkUrl?.isNotEmpty() == true) intent.data = deepLinkUrl.toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        createNotification(title, message, pendingIntent, this)
    }
    
    companion object {
        private val TAG = MyFcmMessageListenerService::class.simpleName

        fun createNotification(title: String?, message: String?, pendingIntent: PendingIntent, context: Context) {
            try {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val notificationBuilder = NotificationCompat.Builder(context, AFInAppEventParameterName.NOTIFICATION_CHANNEL_NOTIFICATIONS).apply {
                    setSmallIcon(R.drawable.shortcuticon)
                    setContentTitle(title)
                    setContentText(message)
                    val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setSound(soundUri)
                    setAutoCancel(true)
                    setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification_round))
                    setContentIntent(pendingIntent)
                }.build()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1001, notificationBuilder)
            } catch (e: Exception) {
                Log.e(TAG, "sendNotification: ${e.message}", e)
            }
        }

    }

}