package com.bytemedrive.network

sealed class Endpoint(val url: String) {
    object EVENTS : Endpoint("customers/{usernameSha3}/events") {
        fun buildUrl(usernameSha3: String): String = url.replace("{usernameSha3}", usernameSha3)
    }

    object EVENTS_WITH_OFFSET : Endpoint("customers/{usernameSha3}/events?offset={offset}") {
        fun buildUrl(usernameSha3: String, offset: Int): String = url.replace("{usernameSha3}", usernameSha3).replace("{offset}", offset.toString())
    }

    object CUSTOMER : Endpoint("customers/{usernameSha3}") {
        fun buildUrl(usernameSha3: String): String = url.replace("{usernameSha3}", usernameSha3)
    }

    object PUBLIC_KEYS : Endpoint("customers/{usernameSha3}/public-keys") {
        fun buildUrl(usernameSha3: String): String = url.replace("{usernameSha3}", usernameSha3)
    }

    object FILES : Endpoint("files")

}
