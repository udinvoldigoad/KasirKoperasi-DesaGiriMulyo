package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["created_at_millis"]),
    ],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "amount")
    val amount: Long,
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
