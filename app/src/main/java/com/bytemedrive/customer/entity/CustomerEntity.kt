package com.bytemedrive.customer.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "customer")
data class CustomerEntity(
    @PrimaryKey
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "wallet") val wallet: UUID?,
    @ColumnInfo(name = "sign_up_at") val signUpAt: ZonedDateTime?,
    @ColumnInfo(name = "balance_gbm") val balanceGbm: Long?
)
