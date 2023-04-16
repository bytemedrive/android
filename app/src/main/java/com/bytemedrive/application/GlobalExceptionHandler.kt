package com.bytemedrive.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.UncaughtExceptionHandler

object GlobalExceptionHandler : UncaughtExceptionHandler {

    private val _throwable = MutableStateFlow<Throwable?>(null)
    val throwable: StateFlow<Throwable?> = _throwable

    override fun uncaughtException(t: Thread, e: Throwable) {
        _throwable.value = e
    }

    fun clear() {
        _throwable.value = null
    }
}