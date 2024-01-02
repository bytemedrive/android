package com.bytemedrive.wallet.payment.creditcode

import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.store.Convertable
import java.util.UUID

data class EventCouponRedeemed(
    val wallet: UUID,
    val code: String
) : Convertable {

    override suspend fun convert(database: ByteMeDatabase) {
    }
}
