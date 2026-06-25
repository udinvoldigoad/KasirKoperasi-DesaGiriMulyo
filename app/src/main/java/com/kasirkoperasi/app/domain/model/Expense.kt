package com.kasirkoperasi.app.domain.model

data class Expense(
    val id: Long = 0L,
    val title: String,
    val amount: Long,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
)
