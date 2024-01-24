package com.bytemedrive.customer.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "customer")
data class CustomerEntity(
    @PrimaryKey
    val username: String,
    val wallet: UUID?,
    val signUpAt: ZonedDateTime?,
    val balanceGbm: Long?
)
