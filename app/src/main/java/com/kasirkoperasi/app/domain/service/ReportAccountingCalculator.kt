package com.kasirkoperasi.app.domain.service

import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.model.SalesReturn
import com.kasirkoperasi.app.domain.model.SalesTransaction
import kotlin.math.min

object ReportAccountingCalculator {
    fun calculateSummary(
        transactions: List<SalesTransaction>,
        returns: List<SalesReturn>,
        debtPayments: List<DebtPayment>,
        lowStockItemCount: Int,
    ): ReportSummary {
        val grossSales = transactions.sumOf { it.totalAmount }
        val grossProfit = transactions.sumOf { it.totalProfit }
        val grossItemCount = transactions.sumOf { it.itemCount }
        val returnAmount = returns.sumOf { it.refundAmount }
        val returnProfit = returns.sumOf { it.profitReduction }
        val returnQuantity = returns.sumOf { it.quantity }
        val returnPaymentAdjustment = calculateReturnPaymentAdjustment(returns)

        val grossCash = transactions.sumOf { transaction ->
            if (transaction.paidPaymentMethod.equals(PAYMENT_METHOD_CASH, ignoreCase = true)) {
                (transaction.paidAmount - transaction.changeAmount).coerceAtLeast(0L)
            } else {
                0L
            }
        } + debtPayments
            .filter { it.paymentMethod.equals(PAYMENT_METHOD_CASH, ignoreCase = true) }
            .sumOf { it.amount }

        val grossQris = transactions
            .filter { it.paidPaymentMethod.equals(PAYMENT_METHOD_QRIS, ignoreCase = true) }
            .sumOf { it.paidAmount } + debtPayments
            .filter { it.paymentMethod.equals(PAYMENT_METHOD_QRIS, ignoreCase = true) }
            .sumOf { it.amount }

        val grossDebt = transactions.sumOf { it.debtAmount }

        return ReportSummary(
            totalSales = grossSales - returnAmount,
            totalProfit = grossProfit - returnProfit,
            totalCash = (grossCash - returnPaymentAdjustment.cashRefund).coerceAtLeast(0L),
            totalQris = (grossQris - returnPaymentAdjustment.qrisRefund).coerceAtLeast(0L),
            totalDebt = (grossDebt - returnPaymentAdjustment.debtReduction).coerceAtLeast(0L),
            soldItemCount = (grossItemCount - returnQuantity).coerceAtLeast(0),
            lowStockItemCount = lowStockItemCount,
        )
    }

    private fun calculateReturnPaymentAdjustment(returns: List<SalesReturn>): ReturnPaymentAdjustment {
        var cashRefund = 0L
        var qrisRefund = 0L
        var debtReduction = 0L

        returns.groupBy { it.transactionId }.values.forEach { transactionReturns ->
            val firstReturn = transactionReturns.first()
            val totalRefund = transactionReturns.sumOf { it.refundAmount }

            when {
                firstReturn.paymentMethod.equals(PAYMENT_METHOD_CASH, ignoreCase = true) -> {
                    cashRefund += totalRefund
                }

                firstReturn.paymentMethod.equals(PAYMENT_METHOD_QRIS, ignoreCase = true) -> {
                    qrisRefund += totalRefund
                }

                firstReturn.paymentMethod.equals(PAYMENT_METHOD_DEBT, ignoreCase = true) -> {
                    val reducedDebt = min(totalRefund, firstReturn.transactionDebtAmount)
                    val paidRefund = (totalRefund - reducedDebt).coerceAtLeast(0L)

                    debtReduction += reducedDebt
                    when {
                        firstReturn.paidPaymentMethod.equals(PAYMENT_METHOD_QRIS, ignoreCase = true) -> {
                            qrisRefund += paidRefund
                        }

                        firstReturn.paidPaymentMethod.equals(PAYMENT_METHOD_CASH, ignoreCase = true) -> {
                            cashRefund += paidRefund
                        }
                    }
                }
            }
        }

        return ReturnPaymentAdjustment(
            cashRefund = cashRefund,
            qrisRefund = qrisRefund,
            debtReduction = debtReduction,
        )
    }

    private data class ReturnPaymentAdjustment(
        val cashRefund: Long,
        val qrisRefund: Long,
        val debtReduction: Long,
    )

    private const val PAYMENT_METHOD_CASH = "Cash"
    private const val PAYMENT_METHOD_QRIS = "QRIS"
    private const val PAYMENT_METHOD_DEBT = "Hutang"
}
