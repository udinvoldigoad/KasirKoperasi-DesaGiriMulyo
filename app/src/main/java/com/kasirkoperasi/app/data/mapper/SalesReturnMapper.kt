package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.SalesReturnReportEntity
import com.kasirkoperasi.app.data.local.entity.SalesReturnSummaryEntity
import com.kasirkoperasi.app.domain.model.SalesReturn
import com.kasirkoperasi.app.domain.model.SalesReturnSummary

fun SalesReturnSummaryEntity.toDomain(): SalesReturnSummary = SalesReturnSummary(
    transactionItemId = transactionItemId,
    quantity = quantity,
    refundAmount = refundAmount,
)

fun SalesReturnReportEntity.toDomain(): SalesReturn = SalesReturn(
    id = id,
    transactionId = transactionId,
    transactionItemId = transactionItemId,
    productId = productId,
    productName = productName,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    quantity = quantity,
    refundAmount = refundAmount,
    transactionNumber = transactionNumber,
    buyerName = buyerName,
    buyerContact = buyerContact,
    paymentMethod = paymentMethod.toSupportedPaymentMethod(),
    paidPaymentMethod = paidPaymentMethod.toSupportedPaidPaymentMethod(),
    transactionPaidAmount = transactionPaidAmount,
    transactionChangeAmount = transactionChangeAmount,
    transactionDebtAmount = transactionDebtAmount,
    createdAtMillis = createdAtMillis,
)

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
