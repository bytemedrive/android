package com.bytemedrive.config

import java.util.Properties


object ConfigProperty {
    var backendUrl: String = ""

    fun setProperties(properties: Properties) {
        var propertiesUrl = properties.getProperty("backend.url")
        if (propertiesUrl.endsWith("/")) {
            propertiesUrl = propertiesUrl.substring(0, propertiesUrl.length - 1)
        }
        backendUrl = "${propertiesUrl}/api/"
    }
}