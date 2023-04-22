package com.example.trello.services

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import com.example.trello.features.home.view.BoardsListScreen
import com.example.trello.sharedpreference.SharedPrefKeys
import com.example.trello.sharedpreference.SharedPreferenceHelper
import com.example.trello.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        var listeners:NotificationCallbacks?=null
        fun registerListener(listeners:NotificationCallbacks){
            this.listeners=listeners
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        SharedPreferenceHelper.getInstance().saveString(SharedPrefKeys.fcmToken, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        listeners?.onNotificationReceived()
        val intent = Intent(this.applicationContext,BoardsListScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        NotificationHelper.with(this)
            .title(message.notification?.title.orEmpty())
            .body(message.notification?.body.orEmpty())
            .pendingIntent(PendingIntent.getActivity(this,100,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .showNotification()
    }
}

interface NotificationCallbacks{
    fun onNotificationReceived()
}