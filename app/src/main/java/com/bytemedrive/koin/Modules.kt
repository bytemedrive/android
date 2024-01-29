package com.bytemedrive.koin

import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.database.ByteMeDatabase
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.root.QueueFileDownloadRepository
import com.bytemedrive.file.root.QueueFileUploadRepository
import com.bytemedrive.file.root.UploadViewModel
import com.bytemedrive.file.root.bottomsheet.CreateFolderViewModel
import com.bytemedrive.file.root.bottomsheet.FileBottomSheetContextFileViewModel
import com.bytemedrive.file.root.bottomsheet.FileBottomSheetContextFolderViewModel
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.control.FileListItemRepository
import com.bytemedrive.file.shared.preview.FilePreviewViewModel
import com.bytemedrive.file.shared.selection.FileSelectionViewModel
import com.bytemedrive.file.starred.StarredViewModel
import com.bytemedrive.file.starred.bottomsheet.StarredBottomSheetContextFileViewModel
import com.bytemedrive.file.starred.bottomsheet.StarredBottomSheetContextFolderViewModel
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigationViewModel
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.network.HttpClient
import com.bytemedrive.price.PricesRepository
import com.bytemedrive.service.ServiceManager
import com.bytemedrive.settings.terminateaccount.TerminateAccountViewModel
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.signin.SignInRepository
import com.bytemedrive.signin.SignInViewModel
import com.bytemedrive.signup.SignUpRepository
import com.bytemedrive.signup.SignUpViewModel
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.wallet.payment.AddCreditMethodViewModel
import com.bytemedrive.wallet.payment.creditcard.PaymentMethodCreditCardViewModel
import com.bytemedrive.wallet.payment.creditcode.PaymentMethodCreditCodeViewModel
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoAmountViewModel
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoPaymentViewModel
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val IODispatcher = "IODispatcher"
val DefaultDispatcher = "DefaultDispatcher"
val ExternalScope = "ExternalScope"

val viewModelsModule = module {
    single { FileViewModel(get(named(ExternalScope)), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { StarredViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AppNavigationViewModel(get(), get()) }
    viewModel { TerminateAccountViewModel(get(), get(), get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get(), get()) }
    viewModel { FileBottomSheetContextFolderViewModel(get()) }
    viewModel { FileBottomSheetContextFileViewModel(get()) }
    viewModel { StarredBottomSheetContextFolderViewModel(get()) }
    viewModel { StarredBottomSheetContextFileViewModel(get()) }
    viewModel { FilePreviewViewModel(get()) }
    viewModel { FileSelectionViewModel(get(), get(), get(), get(), get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { UploadViewModel(get(named(ExternalScope)), get(), get(), get()) }
    viewModel { CreateFolderViewModel(get(named(ExternalScope)), get()) }
    viewModel { AddCreditMethodViewModel() }
    viewModel { PaymentMethodCreditCardViewModel(get(), get()) }
    viewModel { PaymentMethodCreditCodeViewModel(get(), get(), get(), get()) }
    viewModel { PaymentMethodCryptoAmountViewModel(get()) }
    viewModel { PaymentMethodCryptoPaymentViewModel(get(), get()) }
}

val databaseModule = module {
    single { ByteMeDatabase.getInstance(androidContext()) }
    single { get<ByteMeDatabase>().fileDownloadDao() }
    single { get<ByteMeDatabase>().fileUploadDao() }
    single { get<ByteMeDatabase>().eventDao() }
    single { get<ByteMeDatabase>().customerDao() }
    single { get<ByteMeDatabase>().dataFileDao() }
    single { get<ByteMeDatabase>().fileListItemDao() }
    single { get<ByteMeDatabase>().folderDao() }
}

val networkModule = module {
    single { HttpClient() }
}

val accountModule = module {
    single(named(IODispatcher)) { Dispatchers.IO }
    single(named(DefaultDispatcher)) { Dispatchers.Default }
    single(named(ExternalScope)) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { AppNavigator() }
    single { CustomerRepository(get()) }
    single { DataFileRepository(get()) }
    single { QueueFileDownloadRepository(get()) }
    single { FileManager(androidApplication(), get(), get(), get(), get()) }
    single { FileRepository(get(named(IODispatcher))) }
    single { FolderRepository(get()) }
    single { FileListItemRepository(get()) }
    single { QueueFileUploadRepository(get()) }
    single { FolderManager() }
    single { PricesRepository(get(named(IODispatcher))) }
    single { ServiceManager() }
    single { SignUpRepository(get(named(IODispatcher))) }
    single { SignInManager(get(), get(), get(), get(), get()) }
    single { SignInRepository(get(named(IODispatcher))) }
    single { StoreRepository(get(named(IODispatcher))) }
    single { WalletRepository(get(named(IODispatcher))) }
}

val storeModule = module {
    single { EventPublisher(get(), get()) }
    single { EventSyncService(get(), get(), get()) }
}