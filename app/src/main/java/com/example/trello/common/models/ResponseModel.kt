package com.example.trello.common.models

data class ResponseModel<T, E>(val data: T?=null, val error: E?=null, var success: Boolean=false) {
    init {
        success = data != null
    }
}