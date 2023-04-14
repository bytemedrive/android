package com.bytemedrive.store

import kotlinx.coroutines.flow.MutableStateFlow

object AppState {

    val events: MutableList<EventObjectWrapper> = mutableListOf()

    val customer = MutableStateFlow<CustomerAggregate?>(null)

    val authorized = MutableStateFlow(false)
}