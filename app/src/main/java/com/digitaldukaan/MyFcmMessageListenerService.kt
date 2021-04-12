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

    companion object {
        private val TAG = MyFcmMessageListenerService::class.simpleName
    }

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
                        CleverTapAPI.createNotification(applicationContext, extras)
                        sendNotification(extras.getString("nt"), extras.getString("nm"), extras.getString("wzrk_dl"))
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
            sendNotification(title, body)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, "onNewToken: $p0")
    }

    private fun sendNotification(title: String?, message: String?, deepLinkUrl: String? = "") {
        val intent = Intent(this, MainActivity::class.java)
        if (deepLinkUrl?.isNotEmpty() == true) intent.data = deepLinkUrl.toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(this, AFInAppEventParameterName.NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(title)
            setContentText(message)
            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            setSound(soundUri)
            setAutoCancel(true)
            setLargeIcon(BitmapFactory.decodeResource(this@MyFcmMessageListenerService.resources,R.mipmap.ic_launcher_round))
            setContentIntent(pendingIntent)
        }.build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notificationBuilder)
    }

}