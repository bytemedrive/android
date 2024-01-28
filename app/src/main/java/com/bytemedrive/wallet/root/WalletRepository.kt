package com.bytemedrive.wallet.root

import android.util.Log
import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

class WalletRepository(
    private val ioDispatcher: CoroutineDispatcher,
) {

    private val TAG = WalletRepository::class.qualifiedName

    suspend fun getWallet(walletId: UUID): Wallet = withContext(ioDispatcher) { httpClient.get("wallets/$walletId").body() }

    suspend fun createWallet(walletId: UUID) = withContext(ioDispatcher) { httpClient.post("wallets/$walletId") }

    suspend fun redeemCoupon(walletId: UUID, couponCode: String) = withContext(ioDispatcher) {
        httpClient.post("wallets/$walletId/redeem-coupon") {
            setBody(object {
                val code: String = couponCode
            })
        }
    }

    suspend fun createMoneroPayment(walletId: UUID, request: MoneroPaymentRequest): MoneroPaymentResponse = withContext(ioDispatcher) {
        httpClient.post("wallets/$walletId/monero-payments") { setBody(request) }.body()
    }

    suspend fun stripePayment(walletId: UUID, request: StripePaymentRequest): StripePaymentResponse = withContext(ioDispatcher) {
        httpClient.post("wallets/$walletId/stripe-payments") { setBody(request) }.body()
    }

    suspend fun deleteFiles(walletId: UUID, ids: List<String>) {
        Log.i(TAG, "Removing all wallet files")

        withContext(ioDispatcher) {
            httpClient.delete("wallets/$walletId/files") {
                setBody(object {
                    val ids = ids
                })
            }
        }
    }
}