package com.bytemedrive.network

sealed class Endpoint(val url: String) {
    object EVENTS : Endpoint("customers/{idHashed}/events") {
        fun buildUrl(idHashed: String): String = url.replace("{idHashed}", idHashed)
    }

    object FILES : Endpoint("files")

}
