package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.DebtPaymentEntity
import com.kasirkoperasi.app.domain.model.DebtPayment

fun DebtPaymentEntity.toDomain(): DebtPayment = DebtPayment(
    id = id,
    buyerName = buyerName,
    buyerContact = buyerContact,
    paymentMethod = paymentMethod,
    amount = amount,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun DebtPayment.toEntity(): DebtPaymentEntity = DebtPaymentEntity(
    id = id,
    buyerName = buyerName,
    buyerContact = buyerContact,
    paymentMethod = paymentMethod,
    amount = amount,
    note = note,
    createdAtMillis = createdAtMillis,
)
