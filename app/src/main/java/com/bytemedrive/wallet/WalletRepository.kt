package com.bytemedrive.wallet

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.net.URL
import java.util.UUID

class WalletRepository {

    suspend fun getWallet(walletId: UUID): Wallet = httpClient.get("wallets/$walletId").body()

    suspend fun createWallet(walletId: UUID) = httpClient.post("wallets/$walletId")

    suspend fun redeemCoupon(walletId: UUID, couponCode: String) = httpClient.post("wallets/$walletId/redeem-coupon") {
        setBody(object {
            val code: String = couponCode
        })
    }
}