package com.bytemedrive.store

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow

object AppState {

    val customer = MutableStateFlow<CustomerAggregate?>(null)

    val authorized = MutableStateFlow(false)

    val title = MutableStateFlow("")

    val topBarComposable = MutableStateFlow<(@Composable (toggleNav: suspend () -> Unit) -> Unit)?>(null)
}