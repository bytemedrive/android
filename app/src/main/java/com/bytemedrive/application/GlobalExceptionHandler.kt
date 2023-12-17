package com.bytemedrive.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.lang.Thread.UncaughtExceptionHandler

object GlobalExceptionHandler : UncaughtExceptionHandler {

    private val _throwable = MutableStateFlow<Throwable?>(null)
    val throwable: StateFlow<Throwable?> = _throwable

    override fun uncaughtException(t: Thread, e: Throwable) {
        _throwable.update { e }
    }

    fun clear() {
        _throwable.update { null }
    }
}