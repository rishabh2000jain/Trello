package com.example.trello.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.trello.R
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

object AppUtil {
    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showSnackBar(activity: Activity, message: String, type: SnackBarType) {
        val snackBar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        val view = snackBar.view
        val params: FrameLayout.LayoutParams = view.layoutParams as LayoutParams
        params.gravity = Gravity.TOP
        when (type) {
            SnackBarType.ERROR -> view.setBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    R.color.red
                )
            )
            SnackBarType.SUCCESS -> view.setBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    R.color.green
                )
            )
            SnackBarType.NORMAL -> view.setBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    R.color.white
                )
            )
        }
        snackBar.show()
        return
    }

    fun <T> log(clazz: Class<T>, message: String) {
        Log.d(clazz.simpleName, message)
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex: Regex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")
        return emailRegex.matches(email)
    }
}

fun <T> String.fromJsonString(clazz: Class<T>): T {
    val gson = Gson()
    return gson.fromJson(this, clazz)
}

fun <T> Any.toJsonString(): String {
    val gson = Gson()
    return gson.toJson(this)
}

fun String.validPhoneNumber():Boolean {
    return Regex("^(\\+91|\\+91\\-|0)?[789]\\d{9}\$").matches(this)
}

enum class SnackBarType {
    ERROR,
    SUCCESS,
    NORMAL
}