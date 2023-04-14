package com.bytemedrive.store

import com.bytemedrive.file.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CustomerAggregate {

    private val _username: MutableStateFlow<String?> = MutableStateFlow(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    fun setUsername(username: String?) {
        _username.update { username }
    }

    var files: MutableList<File> = mutableListOf()

}