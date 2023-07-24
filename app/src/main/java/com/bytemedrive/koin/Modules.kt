package com.bytemedrive.koin

import com.bytemedrive.file.shared.preview.FilePreviewViewModel
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.root.UploadViewModel
import com.bytemedrive.file.root.bottomsheet.CreateFolderViewModel
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.selection.FileSelectionViewModel
import com.bytemedrive.file.starred.StarredViewModel
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarViewModel
import com.bytemedrive.network.HttpClient
import com.bytemedrive.price.PricesRepository
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.signin.SignInRepository
import com.bytemedrive.signin.SignInViewModel
import com.bytemedrive.signup.SignUpRepository
import com.bytemedrive.signup.SignUpViewModel
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.wallet.credit.AddCreditCodeViewModel
import com.bytemedrive.wallet.credit.AddCreditMethodViewModel
import com.bytemedrive.wallet.credit.AddCryptoMethodAmountViewModel
import com.bytemedrive.wallet.credit.AddCryptoMethodPaymentViewModel
import com.bytemedrive.wallet.root.WalletRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    single { FileViewModel(get(), get(), get(), get(), get(), androidContext()) }
    single { StarredViewModel(get(), get(), get(), get(), get()) }
    single { TopBarViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get(), get()) }
    viewModel { FilePreviewViewModel(get()) }
    viewModel { FileSelectionViewModel(get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { UploadViewModel(get(), get(), get()) }
    viewModel { CreateFolderViewModel(get()) }
    viewModel { AddCreditMethodViewModel() }
    viewModel { AddCreditCodeViewModel(get(), get()) }
    viewModel { AddCryptoMethodAmountViewModel(get()) }
    viewModel { AddCryptoMethodPaymentViewModel(get()) }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single { AppNavigator() }
    single { FileManager(get()) }
    single { FolderManager() }
    single { SignUpRepository() }
    single { SignInManager(get(), get()) }
    single { SignInRepository() }
    single { FileRepository() }
    single { StoreRepository() }
    single { WalletRepository() }
    single { PricesRepository() }
}

val storeModule = module {
    single { EventPublisher(get(), get()) }
    single { EventSyncService(get(), get()) }
}