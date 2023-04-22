package com.example.trello.features

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.trello.R
import com.example.trello.common.BaseActivity
import com.example.trello.features.authentication.AuthActivity
import com.example.trello.features.home.view.BoardsListScreen

@SuppressLint("CustomSplashScreen")
class SplashScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if(isLoggedIn()){
            navigateTo(BoardsListScreen::class.java)
        }else{
            navigateTo(AuthActivity::class.java)
        }
        finish()
    }
    override fun activityName(): String = this::class.java.simpleName
}