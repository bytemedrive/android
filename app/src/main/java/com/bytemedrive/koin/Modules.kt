package com.bytemedrive.koin

import com.bytemedrive.authentication.SignInViewModel
import com.bytemedrive.authentication.SignUpViewModel
import com.bytemedrive.event.EventRepository
import com.bytemedrive.file.FileRepository
import com.bytemedrive.file.FileViewModel
import com.bytemedrive.network.HttpClient
import com.bytemedrive.upload.UploadViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { UploadViewModel(get(), get()) }
    viewModel { FileViewModel() }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single { EventRepository(get()) }
    single { FileRepository(get()) }
}