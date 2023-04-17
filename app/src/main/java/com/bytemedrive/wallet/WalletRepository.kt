package com.bytemedrive.wallet

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.net.URL

class WalletRepository {

    suspend fun getWallet(walletId: String): Wallet = httpClient.get("wallets/$walletId").body()

    suspend fun redeemCoupon(walletId: String, couponCode: String) = httpClient.post("wallets/$walletId/redeem-coupon") {
        setBody(object {
            val code: String = couponCode
        })
    }
}