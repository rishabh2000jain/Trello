package com.example.trello.sharedpreference

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceHelper private constructor(){
    private var sharedPreferences:SharedPreferences? = null
    private var sharedPreferencesEditor:SharedPreferences.Editor? = null
    companion object SharedPrefInstanceBuilder{
        private val INSTANCE:SharedPreferenceHelper = SharedPreferenceHelper()

        fun getInstance():SharedPreferenceHelper = INSTANCE

    }

    fun init(context:Context){
        if(sharedPreferences!=null)return;
        sharedPreferences = context.getSharedPreferences("${context.packageName}.MyPref",Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences?.edit()
    }

    fun saveString(key:String,value:String){
        sharedPreferencesEditor?.putString(key,value)
        commit()
    }

    fun getString(key:String):String?{
        return sharedPreferences?.getString(key,"")
    }

    fun saveInt(key:String,value:Int){
        sharedPreferencesEditor?.putInt(key,value)
        commit()
    }

    fun getInt(key:String):Int?{
        return sharedPreferences?.getInt(key,-1)
    }

    fun saveBoolean(key:String,value:Boolean){
        sharedPreferencesEditor?.putBoolean(key,value)
        commit()
    }

    fun getBoolean(key:String):Boolean?{
        return sharedPreferences?.getBoolean(key,false)
    }

    fun saveLong(key:String,value:Long){
        sharedPreferencesEditor?.putLong(key,value)
        commit()
    }

    fun getLong(key:String):Long?{
        return sharedPreferences?.getLong(key,0)
    }



    fun clear(){
        sharedPreferencesEditor?.clear()
        commit()
    }

    private fun commit(){
        sharedPreferencesEditor?.commit()
    }

}
object SharedPrefKeys{
    const val fcmToken:String = "TOKEN_FCM"
    const val userDetails:String = "USER_DETAILS"
}
