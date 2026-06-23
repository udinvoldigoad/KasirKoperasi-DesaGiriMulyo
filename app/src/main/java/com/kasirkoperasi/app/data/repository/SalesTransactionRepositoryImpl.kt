package com.kasirkoperasi.app.data.repository

import androidx.room.withTransaction
import com.kasirkoperasi.app.data.local.dao.ProductDao
import com.kasirkoperasi.app.data.local.dao.SalesTransactionDao
import com.kasirkoperasi.app.data.local.dao.StockDao
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionItemEntity
import com.kasirkoperasi.app.data.local.entity.StockMovementEntity
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment
import com.kasirkoperasi.app.domain.model.StockMovementType
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class SalesTransactionRepositoryImpl(
    private val salesTransactionDao: SalesTransactionDao,
    private val productDao: ProductDao,
    private val stockDao: StockDao,
    private val database: KasirDatabase,
) : SalesTransactionRepository {
    override suspend fun completeTransaction(
        items: List<SalesTransactionDraftItem>,
        payment: SalesTransactionPayment,
    ): Long {
        require(items.isNotEmpty()) { "Keranjang masih kosong" }
        require(items.all { it.quantity > 0 }) { "Jumlah barang harus lebih dari 0" }

        return database.withTransaction {
            val mergedItems = items
                .groupBy { it.productId }
                .map { (productId, groupedItems) ->
                    SalesTransactionDraftItem(
                        productId = productId,
                        quantity = groupedItems.sumOf { it.quantity },
                    )
                }

            val productSnapshots = mergedItems.map { item ->
                val product = productDao.getProductById(item.productId)
                    ?: error("Barang tidak ditemukan")

                if (product.stockQuantity < item.quantity) {
                    error("Stok ${product.name} tidak cukup")
                }

                product to item.quantity
            }

            val createdAtMillis = System.currentTimeMillis()
            val transactionNumber = "TRX-$createdAtMillis"
            val totalAmount = productSnapshots.sumOf { (product, quantity) ->
                product.sellingPrice * quantity
            }
            val totalProfit = productSnapshots.sumOf { (product, quantity) ->
                (product.sellingPrice - product.purchasePrice) * quantity
            }
            val itemCount = productSnapshots.sumOf { (_, quantity) -> quantity }
            val paymentMethod = payment.paymentMethod
            require(
                paymentMethod == PAYMENT_METHOD_CASH ||
                    paymentMethod == PAYMENT_METHOD_QRIS ||
                    paymentMethod == PAYMENT_METHOD_DEBT,
            ) {
                "Metode pembayaran tidak valid"
            }
            val normalizedPaidAmount = when (paymentMethod) {
                PAYMENT_METHOD_QRIS -> totalAmount
                PAYMENT_METHOD_DEBT -> payment.paidAmount.coerceIn(0L, totalAmount)
                else -> payment.paidAmount.coerceAtLeast(0L)
            }
            val paidPaymentMethod = when {
                paymentMethod == PAYMENT_METHOD_CASH -> PAYMENT_METHOD_CASH
                paymentMethod == PAYMENT_METHOD_QRIS -> PAYMENT_METHOD_QRIS
                paymentMethod == PAYMENT_METHOD_DEBT && normalizedPaidAmount > 0L -> {
                    payment.paidPaymentMethod.toSupportedPaidPaymentMethod()
                }
                else -> ""
            }
            val changeAmount = if (paymentMethod == PAYMENT_METHOD_CASH) {
                (normalizedPaidAmount - totalAmount).coerceAtLeast(0L)
            } else {
                0L
            }
            val debtAmount = if (paymentMethod == PAYMENT_METHOD_DEBT) {
                (totalAmount - normalizedPaidAmount).coerceAtLeast(0L)
            } else {
                0L
            }

            if (paymentMethod == PAYMENT_METHOD_CASH && normalizedPaidAmount < totalAmount) {
                error("Uang dibayarkan masih kurang")
            }
            if (paymentMethod == PAYMENT_METHOD_DEBT && payment.buyerName.isBlank()) {
                error("Nama pembeli wajib diisi untuk transaksi hutang")
            }
            if (paymentMethod == PAYMENT_METHOD_DEBT && normalizedPaidAmount >= totalAmount) {
                error("Transaksi sudah lunas. Gunakan metode Cash atau QRIS.")
            }
            if (paymentMethod == PAYMENT_METHOD_DEBT && normalizedPaidAmount > 0L && paidPaymentMethod.isBlank()) {
                error("Pilih metode uang muka hutang")
            }

            val transactionId = salesTransactionDao.insertTransaction(
                SalesTransactionEntity(
                    transactionNumber = transactionNumber,
                    buyerName = payment.buyerName.trim(),
                    buyerContact = payment.buyerContact.trim(),
                    paymentMethod = paymentMethod,
                    paidPaymentMethod = paidPaymentMethod,
                    totalAmount = totalAmount,
                    totalProfit = totalProfit,
                    paidAmount = normalizedPaidAmount,
                    changeAmount = changeAmount,
                    debtAmount = debtAmount,
                    itemCount = itemCount,
                    createdAtMillis = createdAtMillis,
                ),
            )

            val transactionItems = productSnapshots.map { (product, quantity) ->
                SalesTransactionItemEntity(
                    transactionId = transactionId,
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    unit = product.unit,
                    purchasePrice = product.purchasePrice,
                    sellingPrice = product.sellingPrice,
                    quantity = quantity,
                    subtotal = product.sellingPrice * quantity,
                    profit = (product.sellingPrice - product.purchasePrice) * quantity,
                )
            }

            salesTransactionDao.insertItems(transactionItems)

            productSnapshots.forEach { (product, quantity) ->
                val updatedStock = product.stockQuantity - quantity
                productDao.updateStockQuantity(
                    productId = product.id,
                    stockQuantity = updatedStock,
                    updatedAtMillis = createdAtMillis,
                )
                stockDao.insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        type = StockMovementType.OUT.name,
                        quantity = quantity,
                        currentStock = updatedStock,
                        note = "Transaksi $transactionNumber",
                        createdAtMillis = createdAtMillis,
                    ),
                )
            }

            transactionId
        }
    }

    override suspend fun getTransactionsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesTransaction> {
        return salesTransactionDao.getTransactionsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        ).map { it.toDomain() }
    }

    override suspend fun getDebtTransactions(limit: Int): List<SalesTransaction> {
        return salesTransactionDao.getDebtTransactions(limit = limit).map { it.toDomain() }
    }

    override suspend fun getDebtTransactionsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesTransaction> {
        return salesTransactionDao.getDebtTransactionsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        ).map { it.toDomain() }
    }

    override suspend fun getTransactionItems(transactionId: Long): List<SalesTransactionItem> {
        require(transactionId > 0L) { "Transaksi tidak valid" }

        return salesTransactionDao.getItems(transactionId).map { it.toDomain() }
    }

    private companion object {
        const val PAYMENT_METHOD_CASH = "Cash"
        const val PAYMENT_METHOD_QRIS = "QRIS"
        const val PAYMENT_METHOD_DEBT = "Hutang"
    }
}

private fun String.toSupportedPaidPaymentMethod(): String {
    return when {
        equals("QRIS", ignoreCase = true) -> "QRIS"
        equals("Cash", ignoreCase = true) -> "Cash"
        else -> ""
    }
}
