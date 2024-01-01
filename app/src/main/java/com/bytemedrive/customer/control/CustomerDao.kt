package com.bytemedrive.customer.control

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bytemedrive.customer.entity.CustomerEntity

@Dao
interface CustomerDao {

    @Insert
    suspend fun add(vararg customers: CustomerEntity)

    @Update
    suspend fun update(vararg customers: CustomerEntity)

    @Query(value = "select * from customer where username = :username")
    suspend fun getByUsername(username: String): CustomerEntity
}