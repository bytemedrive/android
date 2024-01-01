package com.bytemedrive.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DataFileLinkDao {

    @Query("select id,name from datafilelink where id=:id")
    suspend fun get(id: String): DataFileLinkEntity?

}