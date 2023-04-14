package com.bytemedrive.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppState {

    val events: MutableList<EventObjectWrapper> = mutableListOf()

    val customer = MutableStateFlow<CustomerAggregate?>(null)

    val authorized = MutableStateFlow(false)

    fun loginSuccess(){
        customer.value = CustomerAggregate()
        authorized.value = true
        events.clear()
    }

    fun logout(){
        customer.value = null
        authorized.value = false
        events.clear()
    }
}