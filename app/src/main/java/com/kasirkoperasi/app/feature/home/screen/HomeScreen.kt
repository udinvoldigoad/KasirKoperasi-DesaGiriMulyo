package com.kasirkoperasi.app.feature.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.barcode.Code128BarcodeGenerator
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.feature.product.state.ProductUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray

@Composable
fun HomeScreen(
    uiState: ProductUiState,
    reportSummary: ReportSummary,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onScanBarcode: () -> Unit = {},
    storeName: String = "KasirKoperasi",
    storeLogoUri: String? = null,
    modifier: Modifier = Modifier,
) {
    val products = uiState.products
    val todaySales = reportSummary.totalSales
    val todayProfit = reportSummary.totalProfit
    val profitPercentage = remember(todaySales, todayProfit) {
        calculateProfitPercentage(
            sales = todaySales,
            profit = todayProfit,
        )
    }
    val lowStockProducts by remember(products) {
        derivedStateOf {
            products.filter { it.stockQuantity <= 5 }
        }
    }
    val lowStock = lowStockProducts.size
    var showLowStockSheet by rememberSaveable { mutableStateOf(false) }
    var showSampleBarcodeSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CreamBackground,
        topBar = {
            HomeTopBar(
                storeName = storeName,
                storeLogoUri = storeLogoUri,
            )
        },
        bottomBar = {
            KasirBottomBar(
                selectedRoute = selectedRoute,
                onRouteSelected = onRouteSelected,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 18.dp,
                end = 16.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TodaySalesCard(
                    salesAmount = todaySales,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SmallMetricCard(
                        title = "Profit Hari Ini",
                        value = todayProfit.toRupiah(),
                        caption = "Margin $profitPercentage",
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        modifier = Modifier.weight(1f),
                    )
                    SmallMetricCard(
                        title = "Stok Menipis",
                        value = lowStock.toString(),
                        caption = "$lowStock barang perlu dicek",
                        icon = Icons.Outlined.Inventory2,
                        showChevron = true,
                        onClick = { showLowStockSheet = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                MenuGrid(
                    onRouteSelected = onRouteSelected,
                    onScanBarcode = onScanBarcode,
                )
            }

            item {
                NewTransactionButton(
                    onClick = { onRouteSelected(AppRoute.Transaction.route) },
                )
            }

            item {
                SampleBarcodeButton(
                    onClick = { showSampleBarcodeSheet = true },
                )
            }
        }
    }

    if (showLowStockSheet) {
        LowStockSheet(
            products = lowStockProducts,
            onDismiss = { showLowStockSheet = false },
        )
    }

    if (showSampleBarcodeSheet) {
        SampleBarcodeSheet(
            barcodeValue = SAMPLE_BARCODE_VALUE,
            onDismiss = { showSampleBarcodeSheet = false },
        )
    }
}

@Composable
private fun HomeTopBar(
    storeName: String,
    storeLogoUri: String?,
) {
    Surface(
        color = CreamBackground,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KoperasiLogo(
                    logoUri = storeLogoUri,
                    fallbackText = storeName.firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                )

                Text(
                    text = storeName,
                    modifier = Modifier.weight(1f),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF303A34),
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySalesCard(
    salesAmount: Long,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 18.dp)
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 74.dp, bottom = 20.dp)
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 12.dp)
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.09f), CircleShape),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Penjualan Hari Ini",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Total transaksi hari ini",
                            color = Color.White.copy(alpha = 0.76f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(23.dp),
                            tint = Color.White,
                        )
                    }
                }

                Text(
                    text = salesAmount.toRupiah(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SmallMetricCard(
    title: String,
    value: String,
    caption: String,
    icon: ImageVector,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cardModifier = if (onClick != null) {
        modifier
            .height(124.dp)
            .clickable { onClick() }
    } else {
        modifier.height(124.dp)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MutedText,
                    )
                    if (showChevron) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = DeepGreen,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = caption,
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LowStockSheet(
    products: List<Product>,
    onDismiss: () -> Unit,
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
                .fillMaxHeight(0.72f)
                .clickable { },
            color = CreamBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HomePanelHandle(onDismiss = onDismiss)

                Text(
                    text = "Stok Menipis",
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Barang dengan stok 5 atau kurang.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                if (products.isEmpty()) {
                    EmptyLowStockCard()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = products,
                            key = { it.id },
                        ) { product ->
                            LowStockProductRow(product = product)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePanelHandle(
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
private fun EmptyLowStockCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Text(
            text = "Tidak ada barang dengan stok menipis.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LowStockProductRow(product: Product) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SoftGray, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = product.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = product.name,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = product.category,
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    text = "${product.stockQuantity} ${product.unit}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun MenuGrid(
    onRouteSelected: (String) -> Unit,
    onScanBarcode: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MenuCard(
                title = "PRODUK",
                icon = Icons.Outlined.ShoppingCart,
                onClick = { onRouteSelected(AppRoute.Product.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Scan\nBarcode",
                icon = Icons.Outlined.QrCodeScanner,
                onClick = onScanBarcode,
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Transaksi",
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                onClick = { onRouteSelected(AppRoute.Transaction.route) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MenuCard(
                title = "Riwayat",
                icon = Icons.Outlined.History,
                onClick = { onRouteSelected(AppRoute.History.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Laporan",
                icon = Icons.Outlined.BarChart,
                onClick = { onRouteSelected(AppRoute.Report.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Setting",
                icon = Icons.Outlined.Settings,
                onClick = { onRouteSelected(AppRoute.Settings.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SoftGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(31.dp),
                    tint = DeepGreen,
                )
            }

            Text(
                text = title,
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NewTransactionButton(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = DeepGreen,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.AddCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Transaksi Baru",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SampleBarcodeButton(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LineSoft),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = DeepGreen,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Generate Barcode Contoh",
                color = DeepGreen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SampleBarcodeSheet(
    barcodeValue: String,
    onDismiss: () -> Unit,
) {
    val barcodeBitmap = remember(barcodeValue) {
        Code128BarcodeGenerator.generate(barcodeValue)
    }

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
                .fillMaxHeight(0.54f)
                .clickable { },
            color = CreamBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HomePanelHandle(onDismiss = onDismiss)

                Text(
                    text = "Barcode Contoh",
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Kode contoh: $barcodeValue. Pakai ini untuk menguji fitur scan kamera.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LineSoft),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            bitmap = barcodeBitmap.asImageBitmap(),
                            contentDescription = "Barcode $barcodeValue",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(128.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Text(
                            text = barcodeValue,
                            color = Color(0xFF17221B),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComingSoonScreen(
    title: String,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CreamBackground,
        topBar = {
            SimpleTopBar(title = title)
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
                .padding(innerPadding)
                .padding(18.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LineSoft),
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = DeepGreen,
                    )
                    Text(
                        text = "$title sedang disiapkan",
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Kita isi halaman ini setelah Beranda sudah pas.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleTopBar(title: String) {
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
            KoperasiLogo()
            Text(
                text = title,
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

private fun Long.toRupiah(): String {
    val grouped = toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "Rp$grouped"
}

private const val SAMPLE_BARCODE_VALUE = "0001"

private fun calculateProfitPercentage(
    sales: Long,
    profit: Long,
): String {
    if (sales <= 0L) return "0%"

    val percentage = (profit * 100) / sales
    return "$percentage%"
}
