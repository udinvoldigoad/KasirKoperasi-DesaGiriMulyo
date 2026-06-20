package com.kasirkoperasi.app.feature.transaction.screen

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kasirkoperasi.app.core.common.AppResult
import com.kasirkoperasi.app.core.image.ProductImageStore
import com.kasirkoperasi.app.core.printer.BluetoothEscPosPrinter
import com.kasirkoperasi.app.core.printer.ReceiptPrintData
import com.kasirkoperasi.app.core.printer.ReceiptPrintItem
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.feature.transaction.state.CartItem
import com.kasirkoperasi.app.feature.transaction.state.PaymentMethod
import com.kasirkoperasi.app.feature.transaction.state.TransactionUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import com.google.android.gms.common.api.ApiException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TransactionScreen(
    products: List<Product>,
    uiState: TransactionUiState,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddProduct: (Product) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onIncreaseQuantity: (Long) -> Unit,
    onDecreaseQuantity: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onBuyerNameChange: (String) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onUseExactAmount: () -> Unit,
    onCompleteTransaction: () -> Unit,
    onClearMessage: () -> Unit,
    onTransactionSaved: () -> Unit,
    modifier: Modifier = Modifier,
    storeName: String = "KasirKoperasi",
    printerName: String = THERMAL_PRINTER_NAME,
    storeLogoUri: String? = null,
) {
    var activeSheet by remember { mutableStateOf<TransactionSheet?>(null) }
    var isSuccessDialogVisible by remember { mutableStateOf(false) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var outOfStockMessage by remember { mutableStateOf<String?>(null) }
    var printMessage by remember { mutableStateOf<String?>(null) }
    var printErrorMessage by remember { mutableStateOf<String?>(null) }
    var isPrintingReceipt by remember { mutableStateOf(false) }
    var pendingPrintData by remember { mutableStateOf<ReceiptPrintData?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val printer = remember(context) { BluetoothEscPosPrinter(context) }
    val runPrintReceipt: (ReceiptPrintData) -> Unit = { receiptData ->
        coroutineScope.launch {
            isPrintingReceipt = true
            printMessage = null
            printErrorMessage = null

            when (val result = printer.printReceipt(receiptData)) {
                is AppResult.Success -> {
                    printMessage = "Struk berhasil dikirim ke printer"
                }

                is AppResult.Error -> {
                    printErrorMessage = result.message
                }
            }

            isPrintingReceipt = false
        }
    }
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val receiptData = pendingPrintData
        pendingPrintData = null

        if (isGranted && receiptData != null) {
            runPrintReceipt(receiptData)
        } else {
            printErrorMessage = "Izin Bluetooth diperlukan untuk mencetak struk"
        }
    }
    val barcodeScanner = remember(context) {
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

    val filteredProducts by remember(products, uiState.searchQuery) {
        derivedStateOf {
            val filtered = products.filter { product ->
                product.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    product.barcode.orEmpty().contains(uiState.searchQuery, ignoreCase = true)
            }

            val (availableProducts, outOfStockProducts) = filtered.partition { it.stockQuantity > 0 }
            availableProducts + outOfStockProducts
        }
    }
    val cartQuantityByProductId by remember(uiState.cartItems) {
        derivedStateOf {
            uiState.cartItems.associate { it.product.id to it.quantity }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            activeSheet = null
            isSuccessDialogVisible = true
            printMessage = null
            printErrorMessage = null
            onTransactionSaved()
        }
    }

    LaunchedEffect(uiState.cartItems.isEmpty()) {
        if (uiState.cartItems.isEmpty() && activeSheet == TransactionSheet.Cart) {
            activeSheet = null
        }
    }

    LaunchedEffect(outOfStockMessage) {
        if (outOfStockMessage != null) {
            delay(2200)
            outOfStockMessage = null
        }
    }

    val shouldShowSuccessDialog = isSuccessDialogVisible && uiState.successMessage != null
    val contentModifier = if (shouldShowSuccessDialog) {
        Modifier
            .fillMaxSize()
            .blur(10.dp)
    } else {
        Modifier.fillMaxSize()
    }
    val startBarcodeScan: () -> Unit = {
        scanErrorMessage = null
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val scannedValue = barcode.rawValue.orEmpty().trim()
                if (scannedValue.isNotEmpty()) {
                    onBarcodeScanned(scannedValue)
                } else {
                    scanErrorMessage = "Barcode tidak terbaca"
                }
            }
            .addOnFailureListener { exception ->
                scanErrorMessage = if (exception is ApiException) {
                    "Scanner tidak tersedia di perangkat ini"
                } else {
                    exception.message ?: "Gagal membuka scanner"
                }
            }
        Unit
    }

    BackHandler(enabled = shouldShowSuccessDialog) {
        isSuccessDialogVisible = false
        onClearMessage()
    }
    BackHandler(enabled = activeSheet != null) {
        activeSheet = null
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = contentModifier,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = CreamBackground,
                topBar = {
                    TransactionTopBar(logoUri = storeLogoUri)
                },
                bottomBar = {
                    KasirBottomBar(
                        selectedRoute = selectedRoute,
                        onRouteSelected = onRouteSelected,
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 120.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            SearchAndScanRow(
                                value = uiState.searchQuery,
                                onValueChange = {
                                    onSearchChange(it)
                                    onClearMessage()
                                    outOfStockMessage = null
                                },
                                onScanClick = startBarcodeScan,
                            )
                        }

                        uiState.errorMessage?.let { message ->
                            item {
                                MessageCard(
                                    message = message,
                                    isError = true,
                                )
                            }
                        }

                        scanErrorMessage?.let { message ->
                            item {
                                MessageCard(
                                    message = message,
                                    isError = true,
                                )
                            }
                        }

                        item {
                            SectionTitle(title = "Pilih Barang")
                        }

                        if (filteredProducts.isEmpty()) {
                            item {
                                EmptyCard(text = "Barang tidak ditemukan.")
                            }
                        } else {
                            items(
                                items = filteredProducts,
                                key = { "product-${it.id}" },
                            ) { product ->
                                val isOutOfStock = product.stockQuantity <= 0

                                ProductPickCard(
                                    product = product,
                                    cartQuantity = cartQuantityByProductId[product.id] ?: 0,
                                    isOutOfStock = isOutOfStock,
                                    onClick = {
                                        if (isOutOfStock) {
                                            onClearMessage()
                                            outOfStockMessage = "${product.name} sedang kosong. Tambah stok di menu Barang."
                                        } else {
                                            outOfStockMessage = null
                                            onAddProduct(product)
                                        }
                                    },
                                )
                            }
                        }
                    }

                    if (uiState.cartItems.isNotEmpty()) {
                        FloatingCartButton(
                            itemCount = uiState.itemCount,
                            onClick = {
                                onClearMessage()
                                activeSheet = TransactionSheet.Cart
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 24.dp),
                        )
                    }
                }
            }

            activeSheet?.let { sheet ->
                TransactionPanelOverlay(
                    onDismiss = { activeSheet = null },
                ) {
                    when (sheet) {
                        TransactionSheet.Cart -> {
                            CartSheetContent(
                                uiState = uiState,
                                onIncreaseQuantity = onIncreaseQuantity,
                                onDecreaseQuantity = onDecreaseQuantity,
                                onRemoveItem = onRemoveItem,
                                onAddMore = { activeSheet = null },
                                onPay = {
                                    onClearMessage()
                                    activeSheet = TransactionSheet.Payment
                                },
                                onDismiss = { activeSheet = null },
                            )
                        }

                        TransactionSheet.Payment -> {
                            PaymentSheetContent(
                                uiState = uiState,
                                onBuyerNameChange = onBuyerNameChange,
                                onPaymentMethodSelected = onPaymentMethodSelected,
                                onPaidAmountChange = onPaidAmountChange,
                                onUseExactAmount = onUseExactAmount,
                                onBackToCart = {
                                    if (uiState.cartItems.isNotEmpty()) {
                                        activeSheet = TransactionSheet.Cart
                                    } else {
                                        activeSheet = null
                                    }
                                },
                                onCompleteTransaction = onCompleteTransaction,
                                onDismiss = { activeSheet = null },
                            )
                        }
                    }
                }
            }
        }

        outOfStockMessage?.let { message ->
            FloatingStockMessage(
                message = message,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 16.dp, end = 16.dp),
            )
        }

        if (shouldShowSuccessDialog) {
            TransactionSuccessDialog(
                storeName = storeName,
                items = uiState.completedItems,
                buyerName = uiState.completedBuyerName,
                paymentMethod = uiState.completedPaymentMethod,
                paidAmount = uiState.completedPaidAmount,
                changeAmount = uiState.completedChangeAmount,
                totalAmount = uiState.completedTotalAmount,
                isPrinting = isPrintingReceipt,
                printMessage = printMessage,
                printErrorMessage = printErrorMessage,
                printerName = printerName,
                onPrint = {
                    val receiptData = uiState.toReceiptPrintData(storeName)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        pendingPrintData = receiptData
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        runPrintReceipt(receiptData)
                    }
                },
                onDismiss = {
                    isSuccessDialogVisible = false
                    onClearMessage()
                },
            )
        }
    }
}

private enum class TransactionSheet {
    Cart,
    Payment,
}

@Composable
private fun TransactionSuccessDialog(
    storeName: String,
    items: List<CartItem>,
    buyerName: String,
    paymentMethod: String,
    paidAmount: Long,
    changeAmount: Long,
    totalAmount: Long,
    isPrinting: Boolean,
    printMessage: String?,
    printErrorMessage: String?,
    printerName: String,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.24f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clickable { },
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(FreshMint, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = DeepGreen,
                    )
                }

                Text(
                    text = "Transaksi Berhasil",
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Data penjualan sudah tersimpan. Cetak struk ke printer $printerName.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                TransactionSuccessDetail(
                    items = items,
                    buyerName = buyerName,
                    paymentMethod = paymentMethod,
                    paidAmount = paidAmount,
                    changeAmount = changeAmount,
                    totalAmount = totalAmount,
                )

                printMessage?.let { message ->
                    PrintStatusMessage(
                        message = message,
                        isError = false,
                    )
                }

                printErrorMessage?.let { message ->
                    PrintStatusMessage(
                        message = message,
                        isError = true,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = "Tutup",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        onClick = onPrint,
                        enabled = !isPrinting,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepGreen,
                            contentColor = Color.White,
                            disabledContainerColor = SoftGray,
                            disabledContentColor = MutedText,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = if (isPrinting) "Mencetak..." else "Print",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionSuccessDetail(
    items: List<CartItem>,
    buyerName: String,
    paymentMethod: String,
    paidAmount: Long,
    changeAmount: Long,
    totalAmount: Long,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SoftGray,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Rincian Barang",
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 178.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = items,
                    key = { "success-item-${it.product.id}" },
                ) { item ->
                    TransactionSuccessItemRow(item = item)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LineSoft),
            )

            if (buyerName.isNotBlank()) {
                SuccessSummaryRow(
                    label = "Pembeli",
                    value = buyerName,
                )
            }

            SuccessSummaryRow(
                label = "Pembayaran",
                value = paymentMethod,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total Harga",
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = totalAmount.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            SuccessSummaryRow(
                label = "Dibayar",
                value = paidAmount.toRupiah(),
            )

            SuccessSummaryRow(
                label = "Kembalian",
                value = changeAmount.toRupiah(),
            )
        }
    }
}

@Composable
private fun SuccessSummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PrintStatusMessage(
    message: String,
    isError: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else FreshMint,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else DeepGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TransactionSuccessItemRow(
    item: CartItem,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.product.name,
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.quantity} ${item.product.unit} x ${item.product.sellingPrice.toRupiah()}",
                color = MutedText,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = item.subtotal.toRupiah(),
            color = DeepGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun TransactionPanelOverlay(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .clickable { },
            color = CreamBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
        ) {
            content()
        }
    }
}

@Composable
private fun FloatingCartButton(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            containerColor = DeepGreen,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp),
            shape = CircleShape,
            color = Color(0xFFE9B44C),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = itemCount.coerceAtMost(99).toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CartSheetContent(
    uiState: TransactionUiState,
    onIncreaseQuantity: (Long) -> Unit,
    onDecreaseQuantity: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onAddMore: () -> Unit,
    onPay: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PanelHandle(onDismiss = onDismiss)
        Text(
            text = "Keranjang",
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = uiState.cartItems,
                key = { "sheet-cart-${it.product.id}" },
            ) { item ->
                CartItemCard(
                    item = item,
                    onIncrease = { onIncreaseQuantity(item.product.id) },
                    onDecrease = { onDecreaseQuantity(item.product.id) },
                    onRemove = { onRemoveItem(item.product.id) },
                )
            }
        }

        CartTotalCard(
            itemCount = uiState.itemCount,
            totalAmount = uiState.totalAmount,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onAddMore,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "Tambah Barang",
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = onPay,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "Bayar",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CartTotalCard(
    itemCount: Int,
    totalAmount: Long,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$itemCount item di keranjang",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "Total Belanja",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = totalAmount.toRupiah(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PaymentSheetContent(
    uiState: TransactionUiState,
    onBuyerNameChange: (String) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onUseExactAmount: () -> Unit,
    onBackToCart: () -> Unit,
    onCompleteTransaction: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PanelHandle(onDismiss = onDismiss)
        Text(
            text = "Pembayaran",
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = uiState.buyerName,
            onValueChange = onBuyerNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama pembeli untuk struk") },
            placeholder = { Text("Contoh: Pak Budi") },
            supportingText = {
                Text("Opsional, tapi sebaiknya diisi agar nama pembeli tampil di struk.")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepGreen,
                unfocusedBorderColor = LineSoft,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = DeepGreen,
            ),
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, LineSoft),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total Harga",
                    modifier = Modifier.weight(1f),
                    color = MutedText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = uiState.totalAmount.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Metode Pembayaran",
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(PaymentMethod.Cash, PaymentMethod.Qris).forEach { method ->
                PaymentMethodButton(
                    method = method,
                    isSelected = uiState.selectedPaymentMethod == method,
                    onClick = { onPaymentMethodSelected(method) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.selectedPaymentMethod == PaymentMethod.Cash) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.paidAmountText,
                    onValueChange = onPaidAmountChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Uang dibayarkan") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepGreen,
                        unfocusedBorderColor = LineSoft,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = DeepGreen,
                    ),
                )
                Button(
                    onClick = onUseExactAmount,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FreshMint,
                        contentColor = DeepGreen,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Uang Pas",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            ChangeInfoCard(
                uiState = uiState,
            )
        }

        uiState.errorMessage?.let { message ->
            MessageCard(
                message = message,
                isError = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBackToCart,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "Keranjang",
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = onCompleteTransaction,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (uiState.isSaving) "Menyimpan..." else "Simpan",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun PanelHandle(
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(onDismiss) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        dragDistance += dragAmount
                        if (dragDistance > 56f) {
                            change.consume()
                            onDismiss()
                        }
                    },
                    onDragEnd = { dragDistance = 0f },
                    onDragCancel = { dragDistance = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(6.dp)
                .background(MutedText, RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun PaymentMethodButton(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DeepGreen else Color.White,
            contentColor = if (isSelected) Color.White else DeepGreen,
        ),
        border = BorderStroke(1.dp, if (isSelected) DeepGreen else LineSoft),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        val icon = when (method) {
            PaymentMethod.Cash -> Icons.Outlined.AttachMoney
            PaymentMethod.Qris -> Icons.Outlined.QrCodeScanner
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = method.label,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChangeInfoCard(
    uiState: TransactionUiState,
) {
    val isCashShort = uiState.selectedPaymentMethod == PaymentMethod.Cash &&
        uiState.paidAmount in 1 until uiState.totalAmount
    val title = if (isCashShort) "Uang Kurang" else "Kembalian"
    val amount = if (isCashShort) uiState.totalAmount - uiState.paidAmount else uiState.changeAmount
    val color = if (isCashShort) MaterialTheme.colorScheme.error else DeepGreen

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = MutedText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = amount.toRupiah(),
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TransactionTopBar(
    logoUri: String?,
) {
    Surface(
        color = CreamBackground,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KoperasiLogo(logoUri = logoUri)
            Text(
                text = "Transaksi",
                modifier = Modifier.weight(1f),
                color = DeepGreen,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun SearchAndScanRow(
    value: String,
    onValueChange: (String) -> Unit,
    onScanClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
        )
        Surface(
            modifier = Modifier
                .size(58.dp)
                .clickable { onScanClick() },
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, LineSoft),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan barcode",
                    modifier = Modifier.size(26.dp),
                    tint = DeepGreen,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(58.dp),
        placeholder = {
            Text(
                text = "Cari barang...",
                color = MutedText,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = DeepGreen,
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Hapus pencarian",
                        modifier = Modifier.size(20.dp),
                        tint = MutedText,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DeepGreen,
            unfocusedBorderColor = LineSoft,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = DeepGreen,
        ),
    )
}

@Composable
private fun ProductPickCard(
    product: Product,
    cartQuantity: Int,
    isOutOfStock: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) SoftGray else Color.White,
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isOutOfStock) Color(0xFFE0E2DC) else LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgedBox(
                badge = {
                    if (cartQuantity > 0) {
                        Badge(
                            containerColor = DeepGreen,
                            contentColor = Color.White,
                        ) {
                            Text(
                                text = cartQuantity.toString(),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
            ) {
                ProductInitial(
                    name = product.name,
                    imageUri = product.imageUri,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    color = if (isOutOfStock) MutedText else Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${product.stockQuantity} ${product.unit}",
                    color = if (isOutOfStock) MaterialTheme.colorScheme.error else MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isOutOfStock) FontWeight.Bold else FontWeight.Normal,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.sellingPrice.toRupiah(),
                    color = if (isOutOfStock) MutedText else DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (isOutOfStock) "Stok kosong" else "Tambah",
                    color = if (isOutOfStock) MaterialTheme.colorScheme.error else DeepGreen,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun FloatingStockMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, Color(0xFFFFD6A7)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFFFE8CC), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFB45309),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Stok Kosong",
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = message,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductInitial(
                name = item.product.name,
                imageUri = item.product.imageUri,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.product.name,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.product.sellingPrice.toRupiah(),
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SoftGray,
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RoundIconButton(
                            icon = Icons.Outlined.Remove,
                            onClick = onDecrease,
                        )
                        Text(
                            text = item.quantity.toString(),
                            modifier = Modifier.width(36.dp),
                            color = Color(0xFF17221B),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        RoundIconButton(
                            icon = Icons.Outlined.Add,
                            onClick = onIncrease,
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = item.subtotal.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    modifier = Modifier.clickable { onRemove() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = CircleShape,
        color = Color.White,
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = DeepGreen,
            )
        }
    }
}

@Composable
private fun ProductInitial(
    name: String,
    imageUri: String? = null,
) {
    val bitmap = rememberProductBitmap(imageUri)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SoftGray, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = DeepGreen,
            )
        }
    }
}

@Composable
private fun rememberProductBitmap(imageUri: String?): Bitmap? {
    val context = LocalContext.current
    return remember(context, imageUri) {
        imageUri
            ?.takeIf { it.isNotBlank() }
            ?.let { ProductImageStore.loadBitmap(context = context, imageUri = it) }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF17221B),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                FreshMint
            },
            contentColor = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                DeepGreen
            },
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Long.toRupiah(): String {
    val grouped = toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "Rp$grouped"
}

private fun TransactionUiState.toReceiptPrintData(storeName: String): ReceiptPrintData {
    return ReceiptPrintData(
        storeName = storeName,
        buyerName = completedBuyerName,
        paymentMethod = completedPaymentMethod,
        paidAmount = completedPaidAmount,
        changeAmount = completedChangeAmount,
        totalAmount = completedTotalAmount,
        items = completedItems.map { item ->
            ReceiptPrintItem(
                name = item.product.name,
                quantity = item.quantity,
                unit = item.product.unit,
                sellingPrice = item.product.sellingPrice,
                subtotal = item.subtotal,
            )
        },
    )
}

private const val THERMAL_PRINTER_NAME = "IDY01POS-58B"
