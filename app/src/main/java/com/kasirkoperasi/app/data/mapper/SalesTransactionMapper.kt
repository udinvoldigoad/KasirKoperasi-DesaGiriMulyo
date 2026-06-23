package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionItemEntity
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem

fun SalesTransactionEntity.toDomain(): SalesTransaction = SalesTransaction(
    id = id,
    transactionNumber = transactionNumber,
    buyerName = buyerName,
    buyerContact = buyerContact,
    paymentMethod = paymentMethod.toSupportedPaymentMethod(),
    paidPaymentMethod = paidPaymentMethod.toSupportedPaidPaymentMethod(),
    totalAmount = totalAmount,
    totalProfit = totalProfit,
    paidAmount = if (paymentMethod.isSupportedPaymentMethod()) paidAmount else totalAmount,
    changeAmount = changeAmount,
    debtAmount = if (paymentMethod.equals("Hutang", ignoreCase = true)) debtAmount else 0L,
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
    return equals("Cash", ignoreCase = true) ||
        equals("QRIS", ignoreCase = true) ||
        equals("Hutang", ignoreCase = true)
}

private fun String.toSupportedPaymentMethod(): String {
    return when {
        equals("QRIS", ignoreCase = true) -> "QRIS"
        equals("Hutang", ignoreCase = true) -> "Hutang"
        else -> "Cash"
    }
}

private fun String.toSupportedPaidPaymentMethod(): String {
    return when {
        equals("QRIS", ignoreCase = true) -> "QRIS"
        equals("Cash", ignoreCase = true) -> "Cash"
        else -> ""
    }
}
