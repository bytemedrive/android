package com.bytemedrive.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.event.EventRepository
import com.bytemedrive.privacy.ShaService
import kotlinx.coroutines.launch

class SignInViewModel(private val eventRepository: EventRepository, private val shaService: ShaService) : ViewModel() {
    fun signIn(email: String, password: String) {
        // TODO: store password to some storage (something like cookies?), will be used to encrypt / decrypt
        // TODO: store email to some storage (something like cookies?), used in path for BE requests
        viewModelScope.launch {
            // TODO: load events from BE -> decrypt
            val data = eventRepository.fetch(shaService.hashSha3(email))
        }
    }
}