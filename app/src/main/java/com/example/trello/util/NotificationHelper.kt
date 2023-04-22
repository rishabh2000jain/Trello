package com.example.trello.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.trello.R


class NotificationHelper {

    companion object Builder {
        var context: Context? = null;
        var title: String? = null
        var body: String? = null
        var url: String? = null
        var intent: PendingIntent? = null
        fun with(context: Context): Builder {
            this.context = context
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun body(body: String): Builder {
            this.body = body
            return this
        }
        fun pendingIntent(intent: PendingIntent): Builder {
            this.intent = intent
            return this
        }
        fun image(url: String): Builder {
            this.url = url
            return this
        }

        fun showNotification() {
            assert(context != null)
            assert(title != null)
            val channelId = context!!.resources.getString(R.string.default_notification_channel_id)
            val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(channelId,"trello",NotificationManager.IMPORTANCE_HIGH)
                channel.enableLights(true)
                channel.enableVibration(true)
                notificationManager.createNotificationChannel(channel)
            }

            var notificationBuilder = NotificationCompat.Builder(
                context!!,
                channelId
            ).setContentTitle(title)
            if (body != null) {
                notificationBuilder.setContentText(body)
            }
            if (url != null) {
                notificationBuilder.setLargeIcon(
                    Glide.with(context!!)
                        .asBitmap()
                        .load(url)
                        .submit().get()
                )
            }

            notificationBuilder.setContentIntent(this.intent)
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(alarmSound)
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            notificationBuilder.setSmallIcon(R.drawable.app_icon)
            notificationManager.notify((System.currentTimeMillis()/1000).toInt(),notificationBuilder.build())
        }
    }

}