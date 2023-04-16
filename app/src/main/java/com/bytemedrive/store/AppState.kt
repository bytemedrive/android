package com.bytemedrive.store

import kotlinx.coroutines.flow.MutableStateFlow

object AppState {

    val customer = MutableStateFlow<CustomerAggregate?>(null)

    val authorized = MutableStateFlow(false)

    val title = MutableStateFlow("")
}