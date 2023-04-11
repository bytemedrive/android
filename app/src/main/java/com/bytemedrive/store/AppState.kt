package com.bytemedrive.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppState {

    val events: MutableList<EventObjectWrapper> = mutableListOf()

    val customer = MutableStateFlow<CustomerAggregate?>(null)

    private val _authorized = MutableStateFlow(false)
    val authorized = _authorized.asStateFlow()

}