package com.bytemedrive.customer

import com.bytemedrive.file.File

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

    var username: String? = null

    var files: MutableList<File> = dummyFiles()
}

