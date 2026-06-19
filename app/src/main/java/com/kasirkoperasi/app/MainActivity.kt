package com.kasirkoperasi.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kasirkoperasi.app.core.navigation.AppRoute
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
import com.kasirkoperasi.app.feature.transaction.screen.TransactionScreen
import com.kasirkoperasi.app.feature.transaction.viewmodel.TransactionViewModel
import com.kasirkoperasi.app.feature.transaction.viewmodel.TransactionViewModelFactory
import com.kasirkoperasi.app.ui.theme.KasirKoperasiTheme

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
        )
    }

    private val transactionHistoryViewModel: TransactionHistoryViewModel by viewModels {
        TransactionHistoryViewModelFactory(
            getSalesTransactionsUseCase = appContainer.getSalesTransactionsUseCase,
            getSalesTransactionItemsUseCase = appContainer.getSalesTransactionItemsUseCase,
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
            var selectedRoute by rememberSaveable { mutableStateOf(AppRoute.Home.route) }

            KasirKoperasiTheme {
                when (selectedRoute) {
                    AppRoute.Home.route -> {
                        val productUiState by productViewModel.uiState.collectAsState()
                        val reportUiState by reportViewModel.uiState.collectAsState()

                        HomeScreen(
                            uiState = productUiState,
                            reportSummary = reportUiState.summary,
                            selectedRoute = selectedRoute,
                            onRouteSelected = { selectedRoute = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Product.route -> {
                        val productUiState by productViewModel.uiState.collectAsState()

                        ProductScreen(
                            uiState = productUiState,
                            onSaveProduct = productViewModel::saveProduct,
                            onUpdateProduct = productViewModel::updateProductWithStockIn,
                            onDeleteProduct = productViewModel::deleteProduct,
                            onClearMessage = productViewModel::clearMessage,
                            selectedRoute = selectedRoute,
                            onRouteSelected = { selectedRoute = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.Transaction.route -> {
                        val productUiState by productViewModel.uiState.collectAsState()
                        val transactionUiState by transactionViewModel.uiState.collectAsState()

                        TransactionScreen(
                            products = productUiState.products,
                            uiState = transactionUiState,
                            selectedRoute = selectedRoute,
                            onRouteSelected = { selectedRoute = it },
                            onSearchChange = transactionViewModel::updateSearchQuery,
                            onAddProduct = transactionViewModel::addProduct,
                            onIncreaseQuantity = transactionViewModel::increaseQuantity,
                            onDecreaseQuantity = transactionViewModel::decreaseQuantity,
                            onRemoveItem = transactionViewModel::removeItem,
                            onBuyerNameChange = transactionViewModel::updateBuyerName,
                            onPaymentMethodSelected = transactionViewModel::selectPaymentMethod,
                            onPaidAmountChange = transactionViewModel::updatePaidAmount,
                            onUseExactAmount = transactionViewModel::useExactAmount,
                            onCompleteTransaction = transactionViewModel::completeTransaction,
                            onClearMessage = transactionViewModel::clearMessage,
                            onTransactionSaved = {
                                productViewModel.loadProducts()
                                reportViewModel.loadTodaySummary()
                            },
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
                            onRouteSelected = { selectedRoute = it },
                            onOpenHistory = { selectedRoute = AppRoute.History.route },
                            onRefresh = reportViewModel::loadTodaySummary,
                            onExportPdf = reportViewModel::exportReportPdf,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    AppRoute.History.route -> {
                        val transactionHistoryUiState by transactionHistoryViewModel.uiState.collectAsState()

                        LaunchedEffect(selectedRoute) {
                            transactionHistoryViewModel.loadTransactions()
                        }

                        TransactionHistoryScreen(
                            uiState = transactionHistoryUiState,
                            onRangeSelected = transactionHistoryViewModel::selectRange,
                            onRefresh = { transactionHistoryViewModel.loadTransactions() },
                            onTransactionSelected = transactionHistoryViewModel::openTransactionDetail,
                            onDismissDetail = transactionHistoryViewModel::dismissTransactionDetail,
                            onRouteSelected = { selectedRoute = it },
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
}
