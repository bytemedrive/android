package com.bytemedrive.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(private val signInManager: SignInManager) : ViewModel() {

    var username = MutableStateFlow("")

    var password = MutableStateFlow(charArrayOf())

    fun signIn(onFailure: () -> Job) = effect {
        val username = username.value.trim()
        val password = password.value

        val successfulSignIn = signInManager.signIn(username, password)

        if (!successfulSignIn) {
            onFailure()
        }
    }

    fun validateForm(): String? =
        when {
            (username.value.isEmpty()) -> "Username is required"
            (password.value.isEmpty()) -> "Password is required"

            else -> {
                null
            }
        }

    private fun effect(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }
}