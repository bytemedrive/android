package com.bytemedrive.navigation

import com.bytemedrive.customer.control.CustomerDao
import com.bytemedrive.customer.entity.CustomerEntity
import kotlinx.coroutines.flow.MutableStateFlow

class NavigationModel(
    private val customerDao: CustomerDao
) {

    val customer = MutableStateFlow<CustomerEntity?>(null)



}