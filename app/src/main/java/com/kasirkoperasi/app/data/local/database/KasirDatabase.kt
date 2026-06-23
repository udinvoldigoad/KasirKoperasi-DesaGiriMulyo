package com.kasirkoperasi.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kasirkoperasi.app.data.local.dao.ProductDao
import com.kasirkoperasi.app.data.local.dao.ReportDao
import com.kasirkoperasi.app.data.local.dao.SalesTransactionDao
import com.kasirkoperasi.app.data.local.dao.StockDao
import com.kasirkoperasi.app.data.local.dao.DebtPaymentDao
import com.kasirkoperasi.app.data.local.entity.DebtPaymentEntity
import com.kasirkoperasi.app.data.local.entity.ProductEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionItemEntity
import com.kasirkoperasi.app.data.local.entity.StockMovementEntity

@Database(
    entities = [
        ProductEntity::class,
        StockMovementEntity::class,
        SalesTransactionEntity::class,
        SalesTransactionItemEntity::class,
        DebtPaymentEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
abstract class KasirDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    abstract fun stockDao(): StockDao

    abstract fun reportDao(): ReportDao

    abstract fun salesTransactionDao(): SalesTransactionDao

    abstract fun debtPaymentDao(): DebtPaymentDao

    companion object {
        @Volatile
        private var instance: KasirDatabase? = null

        fun getInstance(context: Context): KasirDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KasirDatabase::class.java,
                    DatabaseConfig.DATABASE_NAME,
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                    )
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN category TEXT NOT NULL DEFAULT 'Obat'",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    UPDATE products
                    SET category = CASE
                        WHEN lower(name) LIKE '%pupuk%' THEN 'Pupuk'
                        WHEN lower(name) LIKE '%benih%' THEN 'Benih'
                        WHEN lower(name) LIKE '%bibit%' THEN 'Benih'
                        WHEN lower(name) LIKE '%obat%' THEN 'Obat'
                        WHEN lower(name) LIKE '%racun%' THEN 'Obat'
                        WHEN lower(name) LIKE '%alat%' THEN 'Alat Tani'
                        ELSE category
                    END
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sales_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `transaction_number` TEXT NOT NULL,
                        `total_amount` INTEGER NOT NULL,
                        `total_profit` INTEGER NOT NULL,
                        `paid_amount` INTEGER NOT NULL,
                        `change_amount` INTEGER NOT NULL,
                        `item_count` INTEGER NOT NULL,
                        `created_at_millis` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_sales_transactions_transaction_number`
                    ON `sales_transactions` (`transaction_number`)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_sales_transactions_created_at_millis`
                    ON `sales_transactions` (`created_at_millis`)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sales_transaction_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `transaction_id` INTEGER NOT NULL,
                        `product_id` INTEGER NOT NULL,
                        `product_name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `purchase_price` INTEGER NOT NULL,
                        `selling_price` INTEGER NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `subtotal` INTEGER NOT NULL,
                        `profit` INTEGER NOT NULL,
                        FOREIGN KEY(`transaction_id`) REFERENCES `sales_transactions`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_sales_transaction_items_transaction_id`
                    ON `sales_transaction_items` (`transaction_id`)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_sales_transaction_items_product_id`
                    ON `sales_transaction_items` (`product_id`)
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN buyer_name TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN payment_method TEXT NOT NULL DEFAULT 'Cash'",
                )
                db.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN debt_amount INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN image_uri TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `debt_payments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `buyer_name` TEXT NOT NULL,
                        `payment_method` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `note` TEXT,
                        `created_at_millis` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_debt_payments_buyer_name`
                    ON `debt_payments` (`buyer_name`)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_debt_payments_created_at_millis`
                    ON `debt_payments` (`created_at_millis`)
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN paid_payment_method TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL(
                    """
                    UPDATE sales_transactions
                    SET paid_payment_method = CASE
                        WHEN lower(payment_method) = 'cash' THEN 'Cash'
                        WHEN lower(payment_method) = 'qris' THEN 'QRIS'
                        WHEN lower(payment_method) = 'hutang' AND paid_amount > 0 THEN 'Cash'
                        ELSE ''
                    END
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN buyer_contact TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL(
                    "ALTER TABLE debt_payments ADD COLUMN buyer_contact TEXT NOT NULL DEFAULT ''",
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_debt_payments_buyer_name_buyer_contact`
                    ON `debt_payments` (`buyer_name`, `buyer_contact`)
                    """.trimIndent(),
                )
            }
        }
    }
}
