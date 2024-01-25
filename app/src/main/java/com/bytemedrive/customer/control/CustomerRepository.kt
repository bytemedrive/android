package com.bytemedrive.customer.control

import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.customer.entity.Customer

class CustomerRepository(
    private val customerDao: CustomerDao
) {

    suspend fun getCustomer() = encryptedSharedPreferences.username?.let { customerDao.getByUsername(it)?.let(::Customer) }
}