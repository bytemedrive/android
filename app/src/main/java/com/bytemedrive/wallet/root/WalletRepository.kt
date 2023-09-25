package com.bytemedrive.wallet.root

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.util.UUID

class WalletRepository {

    suspend fun getWallet(walletId: UUID): Wallet = httpClient.get("wallets/$walletId").body()

    suspend fun createWallet(walletId: UUID) = httpClient.post("wallets/$walletId")

    suspend fun redeemCoupon(walletId: UUID, couponCode: String) = httpClient.post("wallets/$walletId/redeem-coupon") {
        setBody(object {
            val code: String = couponCode
        })
    }

    suspend fun createMoneroPayment(walletId: UUID, request: MoneroPaymentRequest): MoneroPaymentResponse =
        httpClient.post("wallets/$walletId/monero-payments") { setBody(request) }.body()

    suspend fun stripePayment(walletId: UUID, request: StripePaymentRequest): StripePaymentResponse =
        httpClient.post("wallets/$walletId/stripe-payments") { setBody(request) }.body()
}