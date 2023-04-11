package com.bytemedrive.signup

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.time.ZonedDateTime
import java.util.UUID

data class EventCustomerSignedUp(val username: String, val wallet: UUID, val signedUpAt: ZonedDateTime) : Convertable {
    override fun convert(aggregate: CustomerAggregate) {

    }
}
