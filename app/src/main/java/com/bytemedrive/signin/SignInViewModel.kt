package com.bytemedrive.signin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignInViewModel(private val signInManager: SignInManager) : ViewModel() {

    private var _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private var _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun signIn(context: Context, onFailure: () -> Job) = effect {
        val username = _username.value.trim()
        val password = _password.value.toCharArray()

        val successfulSignIn = signInManager.signIn(username, password, context)
        if (!successfulSignIn) {
            onFailure()
        }
    }

    fun validateForm(): String? =
        when {
            (_username.value.isEmpty()) -> "Username is required"
            (_password.value.isEmpty()) -> "Password is required"

            else -> {
                null
            }
        }

    fun setUsername(value: String) {
        _username.value = value
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    private fun effect(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }
}