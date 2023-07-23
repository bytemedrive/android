package com.bytemedrive.wallet

data class Wallet(
    val id: String,
    val balanceGbm: Long,
    val filesSizeBytes: Long
)