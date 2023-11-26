package com.bytemedrive.application

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.bytemedrive.BuildConfig

class ByteMeSharedPreferences(applicationContext: Context) {

    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var backendUrl: String
        get() = sharedPreferences.getString(BACKEND_URL, BuildConfig.BACKEND_URL)!!
        set(value) {
            sharedPreferences.edit().putString(BACKEND_URL, value.trim().let { if (it.endsWith("/")) it else "$it/" }).apply()
            backendUrlLive.postValue(value)
        }

    val backendUrlLive = MutableLiveData(backendUrl)

    fun backendUrlClear() {
        sharedPreferences.edit().remove(BACKEND_URL).apply()
        backendUrlLive.postValue(backendUrl)
    }

    companion object {

        private const val PREFERENCES_NAME = "byteMePreferences"
        private const val BACKEND_URL = "backendUrl"
    }
}