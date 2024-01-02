package com.bytemedrive.signup

import com.bytemedrive.customer.entity.CustomerEntity
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.time.ZonedDateTime
import java.util.UUID

data class EventCustomerSignedUp(
    val username: String,
    val wallet: UUID,
    val signedUpAt: ZonedDateTime
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
        database.customerDao().update(CustomerEntity(username, wallet, signedUpAt, null))
    }
}
