package com.bytemedrive.application

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.lang.Thread.UncaughtExceptionHandler

object GlobalExceptionHandler : UncaughtExceptionHandler {

    var throwable by mutableStateOf<Throwable?>(null)

    override fun uncaughtException(t: Thread, e: Throwable) {
        throwable = e
    }

    fun clear() {
        throwable = null
    }
}