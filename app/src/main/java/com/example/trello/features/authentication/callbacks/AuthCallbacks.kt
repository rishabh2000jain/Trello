package com.example.trello.features.authentication.callbacks

import com.example.trello.common.models.User

interface AuthCallbacks {
    fun onAuthenticationSuccess(user: User)
}