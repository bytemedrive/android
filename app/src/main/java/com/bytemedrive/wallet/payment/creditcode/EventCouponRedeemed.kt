package com.bytemedrive.wallet.payment.creditcode

import com.bytemedrive.store.Convertable
import com.bytemedrive.store.CustomerAggregate
import java.util.UUID

data class EventCouponRedeemed(
    val wallet: UUID,
    val code: String
) : Convertable {

    override suspend fun convert(customer: CustomerAggregate) {

    }
}
