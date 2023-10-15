package com.bytemedrive.koin

import com.bytemedrive.database.DatabaseManager
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.FileUploadQueueRepository
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.root.UploadViewModel
import com.bytemedrive.file.root.bottomsheet.CreateFolderViewModel
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.preview.FilePreviewViewModel
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
import com.bytemedrive.wallet.payment.creditcode.PaymentMethodCreditCodeViewModel
import com.bytemedrive.wallet.payment.AddCreditMethodViewModel
import com.bytemedrive.wallet.payment.creditcard.PaymentMethodCreditCardViewModel
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoAmountViewModel
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoPaymentViewModel
import com.bytemedrive.wallet.root.WalletRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    single { FileViewModel(get(), get(), get(), get(), get(), get(), androidContext()) }
    single { StarredViewModel(get(), get(), get(), get(), get()) }
    single { TopBarViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get(), get()) }
    viewModel { FilePreviewViewModel(get()) }
    viewModel { FileSelectionViewModel(get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { UploadViewModel(get()) }
    viewModel { CreateFolderViewModel(get()) }
    viewModel { AddCreditMethodViewModel() }
    viewModel { PaymentMethodCreditCardViewModel(get()) }
    viewModel { PaymentMethodCreditCodeViewModel(get(), get()) }
    viewModel { PaymentMethodCryptoAmountViewModel(get()) }
    viewModel { PaymentMethodCryptoPaymentViewModel(get()) }
}

val databaseModule = module {
    single { DatabaseManager(androidContext()) }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single { AppNavigator() }
    single { FileManager(get(), get()) }
    single { FolderManager() }
    single { SignUpRepository() }
    single { SignInManager(get(), get(), get()) }
    single { SignInRepository() }
    single { FileRepository() }
    single { FileUploadQueueRepository(get()) }
    single { StoreRepository() }
    single { WalletRepository() }
    single { PricesRepository() }
}

val storeModule = module {
    single { EventPublisher(get(), get()) }
    single { EventSyncService(get(), get()) }
}