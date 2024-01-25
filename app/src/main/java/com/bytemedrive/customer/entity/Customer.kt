package com.bytemedrive.customer.entity

import java.time.ZonedDateTime
import java.util.UUID

data class Customer(
    val username: String,
    val walletId: UUID?,
    val signUpAt: ZonedDateTime?,
    val balanceGbm: Long?
) {

    constructor(customerEntity: CustomerEntity) : this(
        customerEntity.username,
        customerEntity.walletId,
        customerEntity.signUpAt,
        customerEntity.balanceGbm
    )
}
