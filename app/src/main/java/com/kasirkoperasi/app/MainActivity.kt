package com.kasirkoperasi.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.pdf.ProductBarcodeSheetPdfExporter
import com.kasirkoperasi.app.di.AppContainer
import com.kasirkoperasi.app.feature.history.screen.TransactionHistoryScreen
import com.kasirkoperasi.app.feature.history.viewmodel.TransactionHistoryViewModel
import com.kasirkoperasi.app.feature.history.viewmodel.TransactionHistoryViewModelFactory
import com.kasirkoperasi.app.feature.home.screen.HomeScreen
import com.kasirkoperasi.app.feature.product.screen.ProductScreen
import com.kasirkoperasi.app.feature.product.viewmodel.ProductViewModel
import com.kasirkoperasi.app.feature.product.viewmodel.ProductViewModelFactory
import com.kasirkoperasi.app.feature.report.screen.ReportScreen
import com.kasirkoperasi.app.feature.report.viewmodel.ReportViewModel
import com.kasirkoperasi.app.feature.report.viewmodel.ReportViewModelFactory
import com.kasirkoperasi.app.feature.settings.screen.SettingsScreen
import com.kasirkoperasi.app.feature.settings.viewmodel.SettingsViewModel
import com.kasirkoperasi.app.feature.settings.viewmodel.SettingsViewModelFactory
import com.kasirkoperasi.app.feature.transaction.screen.TransactionScreen
import com.kasirkoperasi.app.feature.transaction.viewmodel.TransactionViewModel
import com.kasirkoperasi.app.feature.transaction.viewmodel.TransactionViewModelFactory
import com.kasirkoperasi.app.ui.theme.KasirKoperasiTheme
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val appContainer by lazy {
        AppContainer(applicationContext)
    }

    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(
            getProductsUseCase = appContainer.getProductsUseCase,
            saveProductUseCase = appContainer.saveProductUseCase,
            updateProductWithStockInUseCase = appContainer.updateProductWithStockInUseCase,
            deactivateProductUseCase = appContainer.deactivateProductUseCase,
        )
    }

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            completeSalesTransactionUseCase = appContainer.completeSalesTransactionUseCase,
        )
    }

    private val reportViewModel: ReportViewModel by viewModels {
        ReportViewModelFactory(
            getSimpleReportUseCase = appContainer.getSimpleReportUseCase,
            getSalesTransactionsUseCase = appContainer.getSalesTransactionsUseCase,
            exportTransactionReportPdfUseCase = appContainer.exportTransactionReportPdfUseCase,
            getDebtCustomerDetailUseCase = appContainer.getDebtCustomerDetailUseCase,
            getDebtCustomersUseCase = appContainer.getDebtCustomersUseCase,
            recordDebtPaymentUseCase = appContainer.recordDebtPaymentUseCase,
        )
    }

    private val transactionHistoryViewModel: TransactionHistoryViewModel by viewModels {
        TransactionHistoryViewModelFactory(
            getSalesTransactionsUseCase = appContainer.getSalesTransactionsUseCase,
            getSalesTransactionItemsUseCase = appContainer.getSalesTransactionItemsUseCase,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            context = applicationContext,
            importProductsCsvUseCase = appContainer.importProductsCsvUseCase,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        setContent {
            val context = LocalContext.current
            val homeBarcodeScanner = remember(context) {
                val options = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                    )
                    .enableAutoZoom()
                    .build()

                GmsBarcodeScanning.getClient(context, options)
            }
            var routeBackStack by rememberSaveable { mutableStateOf(listOf(AppRoute.Home.route)) }
            val selectedRoute = routeBackStack.lastOrNull() ?: AppRoute.Home.route
            var lastBackPressedAt by remember { mutableStateOf(0L) }
            val productUiState by productViewModel.uiState.collectAsState()
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val productBarcodeSheetPdfExporter = remember(context) {
                ProductBarcodeSheetPdfExporter(context)
            }
            val navigateTo: (String) -> Unit = { route ->
                if (route != selectedRoute) {
                    val previousRoute = routeBackStack.dropLast(1).lastOrNull()
                    routeBackStack = when {
                        route == AppRoute.Home.route -> listOf(AppRoute.Home.route)
                        route == previousRoute -> routeBackStack.dropLast(1)
                        else -> routeBackStack + route
                    }
                }
            }
            val navigateBack: () -> Unit = {
                routeBackStack = if (routeBackStack.size > 1) {
                    routeBackStack.dropLast(1)
                } else {
                    listOf(AppRoute.Home.route)
                }
            }

            LaunchedEffect(selectedRoute) {
                lastBackPressedAt = 0L
            }

            BackHandler {
                if (selectedRoute == AppRoute.History.route) {
                    navigateBack()
                    return@BackHandler
                }

                val now = System.currentTimeMillis()
                if (now - lastBackPressedAt <= EXIT_CONFIRMATION_WINDOW_MS) {
                    finish()
                } else {
                    lastBackPressedAt = now
                    Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(settingsUiState.importCompletedSignal) {
                if (settingsUiState.importCompletedSignal > 0) {
                    productViewModel.loadProducts()
                    reportViewModel.loadTodaySummary()
                }
            }

            val generateProductBarcodeSheet = {
                runCatching {
                    productBarcodeSheetPdfExporter.export(productUiState.products)
                }.onSuccess { uri ->
                    sharePdf(uri)
                }.onFailure { throwable ->
                    Toast.makeText(
                        context,
                        throwable.message ?: "Gagal membuat PDF barcode produk",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                Unit
            }

            val startHomeBarcodeScan = {
                homeBarcodeScanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val scannedValue = barcode.rawValue.orEmpty().trim()
                        if (scannedValue.isNotEmpty()) {
                            transactionViewModel.addProductByBarcode(
                                rawBarcode = scannedValue,
                                products = productUiState.products,
                            )
                            navigateTo(AppRoute.Transaction.route)
                        } else {
                            Toast.makeText(context, "Barcode tidak terbaca", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (exception is ApiException && exception.statusCode == CommonStatusCodes.CANCELED) {
                            return@addOnFailureListener
                        }

                        val message = if (exception is ApiException) {
                            "Scanner tidak tersedia di perangkat ini"
                        } else {
                            exception.message ?: "Gagal membuka scanner"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                Unit
            }

            KasirKoperasiTheme {
                when (selectedRoute) {
                    AppRoute.Home.route -> {
                        val reportUiState by reportViewModel.uiState.collectAsState()

                        HomeScreen(
                            uiState = productUiState,
                            reportSummary = reportUiState.summary,
                            selectedRoute = selectedRoute,
                            onRouteSelected = navigateTo,
                            onScanBarcode = startHomeBarcodeScan,
                            storeName = settingsUiState.storeName,
                            storeLogoUri = settingsUiState.logoUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Product.route -> {
                        ProductScreen(
                            uiState = productUiState,
                            onSaveProduct = productViewModel::saveProduct,
                            onUpdateProduct = productViewModel::updateProductWithStockIn,
                            onDeleteProduct = productViewModel::deleteProduct,
                            onClearMessage = productViewModel::clearMessage,
                            onImageDeletionHandled = productViewModel::clearImageDeletionRequest,
                            selectedRoute = selectedRoute,
                            onRouteSelected = navigateTo,
                            storeLogoUri = settingsUiState.logoUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Transaction.route -> {
                        val transactionUiState by transactionViewModel.uiState.collectAsState()

                        TransactionScreen(
                            products = productUiState.products,
                            uiState = transactionUiState,
                            selectedRoute = selectedRoute,
                            onRouteSelected = navigateTo,
                            onSearchChange = transactionViewModel::updateSearchQuery,
                            onAddProduct = transactionViewModel::addProduct,
                            onBarcodeScanned = { scannedValue ->
                                transactionViewModel.addProductByBarcode(
                                    rawBarcode = scannedValue,
                                    products = productUiState.products,
                                )
                            },
                            onScannedQuantityChange = transactionViewModel::updateScannedQuantity,
                            onConfirmScannedProduct = transactionViewModel::confirmScannedProduct,
                            onDismissScannedProductConfirmation = transactionViewModel::dismissScannedProductConfirmation,
                            onIncreaseQuantity = transactionViewModel::increaseQuantity,
                            onDecreaseQuantity = transactionViewModel::decreaseQuantity,
                            onRemoveItem = transactionViewModel::removeItem,
                            onBuyerNameChange = transactionViewModel::updateBuyerName,
                            onBuyerContactChange = transactionViewModel::updateBuyerContact,
                            onPaymentMethodSelected = transactionViewModel::selectPaymentMethod,
                            onDebtInitialPaymentMethodSelected = transactionViewModel::selectDebtInitialPaymentMethod,
                            onPaidAmountChange = transactionViewModel::updatePaidAmount,
                            onUseExactAmount = transactionViewModel::useExactAmount,
                            onCompleteTransaction = transactionViewModel::completeTransaction,
                            onClearMessage = transactionViewModel::clearMessage,
                            onTransactionSaved = {
                                productViewModel.loadProducts()
                                reportViewModel.loadTodaySummary()
                            },
                            storeName = settingsUiState.storeName,
                            printerName = settingsUiState.selectedPrinterName ?: "IDY01POS-58B",
                            storeLogoUri = settingsUiState.logoUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Report.route -> {
                        val reportUiState by reportViewModel.uiState.collectAsState()

                        LaunchedEffect(reportUiState.exportedPdfUri) {
                            reportUiState.exportedPdfUri?.let { uri ->
                                sharePdf(uri)
                                reportViewModel.clearExportResult()
                            }
                        }

                        ReportScreen(
                            uiState = reportUiState,
                            selectedRoute = selectedRoute,
                            onRouteSelected = navigateTo,
                            onOpenHistory = { navigateTo(AppRoute.History.route) },
                            onRefresh = reportViewModel::loadTodaySummary,
                            onExportPdf = reportViewModel::exportReportPdf,
                            onRecordDebtPayment = reportViewModel::recordDebtPayment,
                            onDebtCustomerSelected = reportViewModel::openDebtCustomerDetail,
                            onDismissDebtCustomerDetail = reportViewModel::dismissDebtCustomerDetail,
                            storeLogoUri = settingsUiState.logoUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Settings.route -> {
                        LaunchedEffect(settingsUiState.restoreCompletedSignal) {
                            if (settingsUiState.restoreCompletedSignal > 0) {
                                Toast.makeText(
                                    context,
                                    "Restore selesai. Aplikasi dimuat ulang.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                delay(900)
                                restartApp()
                            }
                        }

                        SettingsScreen(
                            uiState = settingsUiState,
                            selectedRoute = selectedRoute,
                            onRouteSelected = navigateTo,
                            onSaveStoreName = settingsViewModel::saveStoreName,
                            onLogoSelected = settingsViewModel::saveLogo,
                            onImportCsvSelected = settingsViewModel::importProductsCsv,
                            onGenerateBarcodeSheet = generateProductBarcodeSheet,
                            onCleanupProductImages = {
                                settingsViewModel.cleanupProductImages(
                                    productUiState.products
                                        .mapNotNull { it.imageUri }
                                        .toSet(),
                                )
                            },
                            onBackupData = settingsViewModel::backupData,
                            onRestoreBackupSelected = settingsViewModel::restoreData,
                            onLoadPrinters = settingsViewModel::loadPairedPrinters,
                            onPrinterSelected = settingsViewModel::selectPrinter,
                            onTestPrinter = settingsViewModel::testPrintSelectedPrinter,
                            onPrinterPermissionDenied = settingsViewModel::onPrinterPermissionDenied,
                            onClearMessage = settingsViewModel::clearMessage,
                            canGenerateBarcodeSheet = productUiState.products.any { !it.barcode.isNullOrBlank() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.History.route -> {
                        val transactionHistoryUiState by transactionHistoryViewModel.uiState.collectAsState()
                        val productImageById = remember(productUiState.products) {
                            productUiState.products.associate { product ->
                                product.id to product.imageUri
                            }
                        }

                        LaunchedEffect(selectedRoute) {
                            transactionHistoryViewModel.loadTransactions()
                        }

                        TransactionHistoryScreen(
                            uiState = transactionHistoryUiState,
                            onRangeSelected = transactionHistoryViewModel::selectRange,
                            onRefresh = { transactionHistoryViewModel.loadTransactions() },
                            onTransactionSelected = transactionHistoryViewModel::openTransactionDetail,
                            onDismissDetail = transactionHistoryViewModel::dismissTransactionDetail,
                            onRouteSelected = navigateTo,
                            storeLogoUri = settingsUiState.logoUri,
                            productImageById = productImageById,
                            onBackClick = navigateBack,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    private fun sharePdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Bagikan laporan PDF"))
    }

    private fun restartApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        if (launchIntent != null) {
            startActivity(launchIntent)
        }
        finish()
        Runtime.getRuntime().exit(0)
    }
}

private const val EXIT_CONFIRMATION_WINDOW_MS = 2_000L
