package com.bytemedrive.koin

import com.bytemedrive.file.FileRepository
import com.bytemedrive.file.FileViewModel
import com.bytemedrive.network.HttpClient
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.signin.SignInRepository
import com.bytemedrive.signin.SignInViewModel
import com.bytemedrive.signup.SignUpRepository
import com.bytemedrive.signup.SignUpViewModel
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.file.UploadViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { SignUpViewModel(get(), get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { UploadViewModel(get(), get()) }
    viewModel { FileViewModel() }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single { SignUpRepository() }
    single { SignInManager(get(), get()) }
    single { SignInRepository() }
    single { FileRepository() }
    single { StoreRepository() }
}

val storeModule = module {
    single { EventPublisher(get(), get()) }
    single { EventSyncService(get()) }
}