package com.bytemedrive.koin

import com.bytemedrive.network.RestApiBuilder
import org.koin.dsl.module


val viewModelsModule = module {
}

val networkModule = module {
    single { RestApiBuilder() }
}

val accountModule = module {
}