package com.example.sharedPreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class AppSharedPreferences {

    var pref: SharedPreferences? = null

    var editor: SharedPreferences.Editor? = null

    var PRIVATE_MODE = 0


    private val PREF_NAME = "ChatosPerf"

    private val keyProfileImagePath = "profileImagePath"

    private val KeyUID = "UID"

    @SuppressLint("CommitPrefEdits")
    var context: Context? = null
    @SuppressLint("CommitPrefEdits")
    fun PrefManager(context: Context?) {
        this.context = context
        pref = context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref!!.edit()
    }


    fun insertUID(UID:String)
    {
        editor?.putString(KeyUID,UID)
        editor?.apply()
    }


    fun getUID():String
    {
        return pref?.getString(KeyUID,"").toString()
    }

    fun insertProfileImagePath(profileImagePath:String)
    {
        editor?.putString(keyProfileImagePath,profileImagePath)
        editor?.apply()
    }



    fun getProfileImagePath():String
    {
        return pref?.getString(keyProfileImagePath,"").toString()
    }


    fun clearSession() {
        editor!!.clear()
        editor!!.commit()
    }

}