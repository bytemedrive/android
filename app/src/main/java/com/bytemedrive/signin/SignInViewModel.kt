package com.bytemedrive.signin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(private val signInManager: SignInManager) : ViewModel() {

    var username = MutableStateFlow("")

    var password = MutableStateFlow(charArrayOf())

    val loading = MutableStateFlow(false)

    fun signIn(context: Context, onFailure: () -> Job) = viewModelScope.launch {
        loading.update { true }

        val username = username.value.trim()
        val password = password.value

        val successfulSignIn = signInManager.signIn(username, password, context)

        if (!successfulSignIn) {
            onFailure()
        }

        loading.update { false }
    }

    fun validateForm(): String? =
        when {
            (username.value.isEmpty()) -> "Username is required"
            (password.value.isEmpty()) -> "Password is required"

            else -> {
                null
            }
        }
}