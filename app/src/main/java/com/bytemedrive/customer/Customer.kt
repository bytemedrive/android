package com.bytemedrive.customer

import com.bytemedrive.file.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Customer {

    private var _username: MutableStateFlow<String?> = MutableStateFlow(null)
    val username: StateFlow<String?> = _username

    val authorized = MutableStateFlow(false)

    var files: MutableList<File> = mutableListOf()

    fun setUsername(username: String?) {
        _username.value = username
        authorized.value = username != null
    }
}

