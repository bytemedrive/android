package com.bytemedrive.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class AppNavigationViewModel(
    private val customerRepository: CustomerRepository,
    private val dataFileRepository: DataFileRepository
): ViewModel() {
    var username by mutableStateOf("")
    var usedStorage by mutableStateOf(0.0)
    var balanceGbm by mutableStateOf(0L)

    fun getMenuData() = viewModelScope.launch {
        customerRepository.getCustomer()?.let { customer ->
            username = customer.username
            balanceGbm = customer.balanceGbm ?: 0L
        }

        usedStorage = dataFileRepository.getUsedStorage()?.toDouble() ?: 0.0
    }

}