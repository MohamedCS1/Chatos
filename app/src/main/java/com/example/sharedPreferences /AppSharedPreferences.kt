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

    private val KeyCurrentUserName = "UserName"

    private val KeyJob = "KeyJob"

    private val KeyGender = "KerGender"

    private val KeyCountry = "KeyCountry"

    private val KeyEmail = "KeyEmail"

    @SuppressLint("CommitPrefEdits")
    var context: Context? = null
    @SuppressLint("CommitPrefEdits")
    fun PrefManager(context: Context?) {
        this.context = context
        pref = context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref!!.edit()
    }


    fun insertCurrentUserUID(UID:String)
    {
        editor?.putString(KeyUID,UID)
        editor?.apply()
    }


    fun getCurrentUserUID():String
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

    fun insertCurrentUserName(userName:String)
    {
        editor?.putString(KeyCurrentUserName,userName)
        editor?.apply()
    }

    fun getCurrentUserName():String
    {
        return pref?.getString(KeyCurrentUserName,"").toString()
    }


    fun insertUserJob(job:String)
    {
        editor?.putString(KeyJob,job)
        editor?.apply()
    }

    fun getUserJob():String
    {
        return pref?.getString(KeyJob,"").toString()
    }

    fun insertUserGender(gender:String)
    {
        editor?.putString(KeyGender,gender)
        editor?.apply()
    }

    fun getUserGender():String
    {
        return pref?.getString(KeyGender,"").toString()
    }

    fun insertUserCountry(country:String)
    {
        editor?.putString(KeyCountry,country)
        editor?.apply()
    }

    fun getUserCountry():String
    {
        return pref?.getString(KeyCountry,"").toString()
    }

    fun insertUserEmail(email:String)
    {
        editor?.putString(KeyEmail,email)
        editor?.apply()
    }

    fun getUserEmail():String
    {
        return pref?.getString(KeyEmail,"").toString()
    }

    fun clearSession() {
        editor!!.clear()
        editor!!.commit()
    }

}