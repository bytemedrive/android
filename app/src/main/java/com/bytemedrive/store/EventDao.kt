package com.bytemedrive.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg files: EventEntity)

    @Query("select count(*) from event")
    suspend fun getEventsCount(): Long
}