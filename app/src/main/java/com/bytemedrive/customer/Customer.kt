package com.bytemedrive.customer

import com.bytemedrive.file.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun dummyFiles(): MutableList<File> {
    val files = mutableListOf<File>()

    for (index in 1..200) {
        files.add(File(index.toString(), getRandomString(10), 120000000L, "image/png"))
    }
    return files
}

object Customer {

    private var _username: MutableStateFlow<String?> = MutableStateFlow(null)
    val username: StateFlow<String?> = _username

    val authorized = MutableStateFlow(false)

    var files: MutableList<File> = dummyFiles()

    fun setUsername(username: String?) {
        _username.value = username
        authorized.value = username != null
    }
}

