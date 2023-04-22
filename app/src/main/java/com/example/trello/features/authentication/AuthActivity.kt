package com.example.trello.features.authentication

import android.os.Bundle
import android.util.Log
import com.example.trello.common.BaseActivity
import com.example.trello.common.models.User
import com.example.trello.util.toJsonString
import com.example.trello.databinding.ActivityAuthBinding
import com.example.trello.features.authentication.view.LoginFragment
import com.example.trello.features.authentication.callbacks.AuthCallbacks
import com.example.trello.features.authentication.view.SignupFragment
import com.example.trello.features.home.view.BoardsListScreen
import com.example.trello.sharedpreference.SharedPrefKeys
import com.example.trello.sharedpreference.SharedPreferenceHelper

class AuthActivity : BaseActivity(), AuthCallbacks {
    companion object {
        val TAG:String = AuthActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate")
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleClickEvents()
    }


    private fun handleClickEvents() {
        binding.signupBtn.setOnClickListener {
            showSignupDialog()
        }
        binding.loginBtn.setOnClickListener {
            showLoginDialog()
        }
    }

    private fun showSignupDialog() {
        val signupFragment = SignupFragment()
        signupFragment.registerAuthCallbacks(this)
        signupFragment.show(supportFragmentManager, signupFragment.tag)
    }

    private fun showLoginDialog() {
        val signupFragment = LoginFragment()
        signupFragment.registerAuthCallbacks(this)
        signupFragment.show(supportFragmentManager, signupFragment.tag)
    }

    override fun onAuthenticationSuccess(user: User) {
        if(isLoggedIn()){
            showLoader()
            updateUserDetails(user)
            pushReplacement(BoardsListScreen::class.java)
            hideLoader()
        }
    }

    override fun onBackPressed() {
        handleBackPressed()
    }
    override fun activityName(): String = this::class.java.simpleName
}