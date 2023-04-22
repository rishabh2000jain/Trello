package com.example.trello

import android.app.Application
import com.example.trello.sharedpreference.SharedPreferenceHelper
import com.google.firebase.FirebaseApp

class TrelloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        SharedPreferenceHelper.getInstance().init(this)
    }
}