package io.earthbanc.mrv.config

import java.util.Properties


object ConfigProperty {
    var backendUrl: String = ""

    fun setProperties(properties: Properties) {
        backendUrl = properties.getProperty("backend.url")
    }
}