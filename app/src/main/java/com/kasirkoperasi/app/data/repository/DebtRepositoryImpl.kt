package com.kasirkoperasi.app.data.repository

import com.kasirkoperasi.app.data.local.dao.DebtPaymentDao
import com.kasirkoperasi.app.data.local.dao.SalesTransactionDao
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.data.mapper.toEntity
import com.kasirkoperasi.app.domain.model.DebtCustomerDetail
import com.kasirkoperasi.app.domain.model.DebtCustomerSummary
import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.repository.DebtRepository

class DebtRepositoryImpl(
    private val debtPaymentDao: DebtPaymentDao,
    private val salesTransactionDao: SalesTransactionDao,
) : DebtRepository {
    override suspend fun recordPayment(payment: DebtPayment): Long {
        require(payment.buyerName.isNotBlank()) { "Nama pembeli wajib diisi" }
        require(payment.amount > 0L) { "Nominal pembayaran harus lebih dari 0" }
        require(payment.paymentMethod.equals(PAYMENT_METHOD_CASH, ignoreCase = true) ||
            payment.paymentMethod.equals(PAYMENT_METHOD_QRIS, ignoreCase = true)) {
            "Metode pembayaran hutang tidak valid"
        }

        val remainingDebt = getOutstandingCustomers()
            .firstOrNull {
                it.buyerName.equals(payment.buyerName.trim(), ignoreCase = true) &&
                    it.buyerContact.equals(payment.buyerContact.trim(), ignoreCase = true)
            }
            ?.remainingDebt
            ?: 0L

        require(remainingDebt > 0L) { "Pembeli ini tidak memiliki sisa hutang" }
        require(payment.amount <= remainingDebt) { "Pembayaran melebihi sisa hutang" }

        return debtPaymentDao.insertDebtPayment(
            payment.copy(
                buyerName = payment.buyerName.trim(),
                buyerContact = payment.buyerContact.trim(),
                paymentMethod = payment.paymentMethod.toSupportedPaymentMethod(),
                amount = payment.amount,
            ).toEntity(),
        )
    }

    override suspend fun getPaymentsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<DebtPayment> {
        return debtPaymentDao.getPaymentsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        ).map { it.toDomain() }
    }

    override suspend fun getOutstandingCustomers(): List<DebtCustomerSummary> {
        val transactions = salesTransactionDao.getDebtTransactions(limit = Int.MAX_VALUE)
        val payments = debtPaymentDao.getAllPayments()

        val debtByBuyer = transactions
            .groupBy { it.toDebtIdentityKey() }
            .mapValues { (_, transactions) -> transactions.sumOf { it.debtAmount } }

        val displayByBuyer = transactions
            .associateBy { it.toDebtIdentityKey() }

        val paidByBuyer = payments
            .groupBy { it.toDebtIdentityKey() }
            .mapValues { (_, payments) -> payments.sumOf { it.amount } }

        return debtByBuyer.mapNotNull { (buyerKey, totalDebt) ->
            val totalPaid = paidByBuyer[buyerKey] ?: 0L
            val display = displayByBuyer[buyerKey]
            DebtCustomerSummary(
                buyerName = display?.buyerName?.trim().orEmpty().ifBlank { buyerKey.substringBefore(KEY_SEPARATOR) },
                buyerContact = display?.buyerContact?.trim().orEmpty(),
                totalDebt = totalDebt,
                totalPaid = totalPaid,
            ).takeIf { it.remainingDebt > 0L }
        }.sortedByDescending { it.remainingDebt }
    }

    override suspend fun getCustomerDetail(
        buyerName: String,
        buyerContact: String,
    ): DebtCustomerDetail {
        val targetKey = toDebtIdentityKey(buyerName, buyerContact)
        val transactions = salesTransactionDao.getDebtTransactions(limit = Int.MAX_VALUE)
            .filter { it.toDebtIdentityKey() == targetKey }
        val payments = debtPaymentDao.getAllPayments()
            .filter { it.toDebtIdentityKey() == targetKey }

        val totalDebt = transactions.sumOf { it.debtAmount }
        val totalPaid = payments.sumOf { it.amount }
        val summary = DebtCustomerSummary(
            buyerName = buyerName.trim(),
            buyerContact = buyerContact.trim(),
            totalDebt = totalDebt,
            totalPaid = totalPaid,
        )

        return DebtCustomerDetail(
            summary = summary,
            transactions = transactions.map { it.toDomain() },
            payments = payments.map { it.toDomain() },
        )
    }

    private fun String.toSupportedPaymentMethod(): String {
        return if (equals(PAYMENT_METHOD_QRIS, ignoreCase = true)) PAYMENT_METHOD_QRIS else PAYMENT_METHOD_CASH
    }

    private companion object {
        const val PAYMENT_METHOD_CASH = "Cash"
        const val PAYMENT_METHOD_QRIS = "QRIS"
        const val KEY_SEPARATOR = "\u001F"
    }
}

private fun com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity.toDebtIdentityKey(): String {
    return toDebtIdentityKey(buyerName, buyerContact)
}

private fun com.kasirkoperasi.app.data.local.entity.DebtPaymentEntity.toDebtIdentityKey(): String {
    return toDebtIdentityKey(buyerName, buyerContact)
}

private fun toDebtIdentityKey(
    buyerName: String,
    buyerContact: String,
): String {
    return buyerName.trim().lowercase() + "\u001F" + buyerContact.trim().lowercase()
}
