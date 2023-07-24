package com.bytemedrive.price

import com.bytemedrive.application.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.util.UUID

class PricesRepository {

    suspend fun getPrices(): Prices = httpClient.get("prices").body()

}