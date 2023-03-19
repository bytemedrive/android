package com.bytemedrive.authentication

import kotlinx.serialization.Serializable

@Serializable
data class SignIn(val email: String, val password: CharArray)
