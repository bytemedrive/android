package com.bytemedrive.store

import com.bytemedrive.file.root.File
import com.bytemedrive.folder.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import java.util.UUID

class CustomerAggregate {

    private val _username: MutableStateFlow<String?> = MutableStateFlow(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    fun setUsername(username: String?) {
        _username.update { username }
    }

    var files: MutableList<File> = mutableListOf()

    var folders: MutableList<Folder> = mutableListOf()

    var wallet: UUID? = null

    var signUpAt: ZonedDateTime? = null

    var creditAmount: Long? = null

    override fun toString(): String {
        return "CustomerAggregate(username=${_username.value}, files=$files, wallet=$wallet, signUpAt=$signUpAt)"
    }
}