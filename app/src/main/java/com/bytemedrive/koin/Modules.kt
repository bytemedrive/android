package com.bytemedrive.koin

import com.bytemedrive.authentication.SignInViewModel
import com.bytemedrive.event.EventRepository
import com.bytemedrive.network.RestApiBuilder
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.upload.UploadViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelsModule = module {
    viewModel { SignInViewModel(get(), get(), get(), get()) }
    viewModel { UploadViewModel(get(), get()) }
}

val networkModule = module {
    single { RestApiBuilder() }
}

val accountModule = module {
    single { AesService() }
    single { ShaService() }
    single { EncryptedStorage() }
    single { EventRepository(get()) }
}