package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.ExpenseEntity
import com.kasirkoperasi.app.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    title = title,
    amount = amount,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    note = note,
    createdAtMillis = createdAtMillis,
)
