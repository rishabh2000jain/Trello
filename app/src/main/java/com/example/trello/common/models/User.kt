package com.example.trello.common.models

data class User(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val profile: String?,
    val fcmToken: String?=null
)
