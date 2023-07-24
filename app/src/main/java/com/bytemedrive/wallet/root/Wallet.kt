package com.bytemedrive.wallet.root

data class Wallet(
    val id: String,
    val balanceGbm: Long,
    val filesSizeBytes: Long
)