package com.bytemedrive.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import kotlinx.coroutines.launch

class AppNavigationViewModel(
    private val customerRepository: CustomerRepository,
    private val dataFileRepository: DataFileRepository
): ViewModel() {
    var username by mutableStateOf("")
    var usedStorage by mutableStateOf("")

    init {
        viewModelScope.launch {
            customerRepository.getCustomer()?.let { customer ->
                username = customer.username
            }

            usedStorage = dataFileRepository.getUsedStorage()

        }
    }
}