package com.bytemedrive.store

import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.folder.FolderEntity
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

    var dataFiles = MutableStateFlow<List<DataFileEntity>>(emptyList())

    var dataFilesLinks = MutableStateFlow<List<DataFileLinkEntity>>(emptyList())

    var folders = MutableStateFlow<List<FolderEntity>>(emptyList())

    var wallet: UUID? = null

    var signUpAt: ZonedDateTime? = null

    var balanceGbm: Long? = null

    override fun toString(): String {
        return "CustomerAggregate(username=${_username.value}, wallet=$wallet, wallet=$wallet, signUpAt=$signUpAt)"
    }
}