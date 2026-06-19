package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionItemEntity
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem

fun SalesTransactionEntity.toDomain(): SalesTransaction = SalesTransaction(
    id = id,
    transactionNumber = transactionNumber,
    buyerName = buyerName,
    paymentMethod = paymentMethod.toSupportedPaymentMethod(),
    totalAmount = totalAmount,
    totalProfit = totalProfit,
    paidAmount = if (paymentMethod.isSupportedPaymentMethod()) paidAmount else totalAmount,
    changeAmount = changeAmount,
    itemCount = itemCount,
    createdAtMillis = createdAtMillis,
)

fun SalesTransactionItemEntity.toDomain(): SalesTransactionItem = SalesTransactionItem(
    id = id,
    transactionId = transactionId,
    productId = productId,
    productName = productName,
    category = category,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    quantity = quantity,
    subtotal = subtotal,
    profit = profit,
)

private fun String.isSupportedPaymentMethod(): Boolean {
    return equals("Cash", ignoreCase = true) || equals("QRIS", ignoreCase = true)
}

private fun String.toSupportedPaymentMethod(): String {
    return if (equals("QRIS", ignoreCase = true)) "QRIS" else "Cash"
}
