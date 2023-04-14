package com.bytemedrive.koin

import com.bytemedrive.signin.SignInViewModel
import com.bytemedrive.signin.SingInRepository
import com.bytemedrive.file.FileRepository
import com.bytemedrive.file.FileViewModel
import com.bytemedrive.network.HttpClient
import com.bytemedrive.signup.SignUpRepository
import com.bytemedrive.signup.SignUpViewModel
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.upload.UploadViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { UploadViewModel(get(), get()) }
    viewModel { FileViewModel() }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single { SignUpRepository(get()) }
    single { SingInRepository(get()) }
    single { FileRepository(get()) }
    single { StoreRepository(get()) }
}

val storeModule = module {
    single { EventPublisher(get(), get()) }
    single { EventSyncService(get()) }
}