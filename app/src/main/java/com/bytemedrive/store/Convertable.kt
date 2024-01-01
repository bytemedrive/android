package com.bytemedrive.store

import com.bytemedrive.database.ByteMeDatabase

interface Convertable {

    suspend fun convert(database: ByteMeDatabase)
}