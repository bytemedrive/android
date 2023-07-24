package com.bytemedrive.wallet.root

import java.math.BigDecimal
import java.time.ZonedDateTime

data class MoneroPaymentResponse(val walletAddress: String, val amount: BigDecimal, val expiration: ZonedDateTime)
