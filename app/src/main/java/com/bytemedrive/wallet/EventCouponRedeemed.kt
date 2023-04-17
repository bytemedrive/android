package com.bytemedrive.wallet

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.time.ZonedDateTime
import java.util.UUID

data class EventCouponRedeemed(
    val wallet: UUID,
    val code: String
) : Convertable {

    override fun convert(customer: CustomerAggregate) {

    }
}
