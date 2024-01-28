package com.bytemedrive.store

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow

object AppState {

    val authorized = MutableStateFlow(false)

    val title = MutableStateFlow("")

    val topBarComposable = MutableStateFlow<(@Composable (toggleNav: suspend () -> Unit) -> Unit)?>(null)
}