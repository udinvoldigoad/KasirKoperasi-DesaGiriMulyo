package com.kasirkoperasi.app.feature.home.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.image.ProductImageStore
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.ui.dismissPanelOnTap
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.core.ui.ModalOverlayWindow
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.feature.home.state.HomeReturnUiState
import com.kasirkoperasi.app.feature.product.state.ProductUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: ProductUiState,
    reportSummary: ReportSummary,
    returnUiState: HomeReturnUiState = HomeReturnUiState(),
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onScanBarcode: () -> Unit = {},
    onOpenReturnSheet: () -> Unit = {},
    onReturnTransactionSelected: (SalesTransaction) -> Unit = {},
    onReturnTransactionListRequested: () -> Unit = {},
    onReturnItemSelected: (SalesTransactionItem) -> Unit = {},
    onProcessReturn: () -> Unit = {},
    onReturnSheetDismissed: () -> Unit = {},
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
    var showInfoSheet by rememberSaveable { mutableStateOf(false) }
    var showReturnSheet by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showLowStockSheet || showInfoSheet || showReturnSheet) {
        when {
            showInfoSheet -> showInfoSheet = false
            showReturnSheet -> {
                if (returnUiState.selectedTransaction != null) {
                    onReturnTransactionListRequested()
                } else {
                    showReturnSheet = false
                    onReturnSheetDismissed()
                }
            }
            showLowStockSheet -> showLowStockSheet = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CreamBackground,
        topBar = {
            HomeTopBar(
                storeName = storeName,
                storeLogoUri = storeLogoUri,
                onInfoClick = { showInfoSheet = true },
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
                ReturnProductButton(
                    onClick = {
                        showReturnSheet = true
                        onOpenReturnSheet()
                    },
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

    if (showInfoSheet) {
        HomeInfoSheet(
            onDismiss = { showInfoSheet = false },
        )
    }

    if (showReturnSheet) {
        ReturnProductSheet(
            uiState = returnUiState,
            onTransactionSelected = onReturnTransactionSelected,
            onTransactionListRequested = onReturnTransactionListRequested,
            onItemSelected = onReturnItemSelected,
            onProcessReturn = onProcessReturn,
            onDismiss = {
                showReturnSheet = false
                onReturnSheetDismissed()
            },
        )
    }
}

@Composable
private fun HomeTopBar(
    storeName: String,
    storeLogoUri: String?,
    onInfoClick: () -> Unit,
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
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DeepGreen.copy(alpha = 0.10f))
                        .clickable { onInfoClick() },
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

private data class HomeGuideItem(
    val title: String,
    val description: String,
    val steps: List<String>,
    val icon: ImageVector,
)

private fun homeGuideItems(): List<HomeGuideItem> = listOf(
    HomeGuideItem(
        title = "Mulai Transaksi",
        description = "Dipakai kasir saat melayani pembeli.",
        steps = listOf(
            "Buka menu Transaksi.",
            "Pilih barang atau scan barcode rak.",
            "Cek isi keranjang, lalu tekan Bayar.",
            "Pilih Cash, QRIS, atau Hutang sesuai pembayaran.",
            "Setelah berhasil, gunakan tombol Print jika printer sudah tersambung.",
        ),
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
    ),
    HomeGuideItem(
        title = "Data Barang dan Stok",
        description = "Semua produk, harga, foto, barcode, dan stok dikelola dari menu Produk.",
        steps = listOf(
            "Tambah produk baru dari menu Produk.",
            "Isi nama, kategori, harga beli, harga jual, stok, satuan, dan barcode 4 angka.",
            "Klik kartu barang untuk mengedit data atau menambah stok masuk.",
            "Barang stok kosong akan dibuat abu-abu di Transaksi dan tidak bisa masuk keranjang.",
        ),
        icon = Icons.Outlined.Inventory2,
    ),
    HomeGuideItem(
        title = "Barcode Rak",
        description = "Barcode dipakai untuk mempercepat pemilihan barang.",
        steps = listOf(
            "Setiap produk punya kode barcode 4 angka.",
            "Cetak barcode massal dari Pengaturan lalu tempel di rak.",
            "Tombol Scan Barcode di Beranda atau Transaksi akan mencari produk berdasarkan kode itu.",
            "Jika kode tidak terdaftar, aplikasi menampilkan pesan barang belum terdaftar.",
        ),
        icon = Icons.Outlined.QrCodeScanner,
    ),
    HomeGuideItem(
        title = "Hutang dan Pelunasan",
        description = "Hutang dicatat agar sisa pembayaran pembeli tetap terlihat di laporan.",
        steps = listOf(
            "Pilih metode Hutang saat pembeli belum membayar penuh.",
            "Nama pembeli wajib diisi untuk transaksi hutang.",
            "Nominal yang dibayar boleh berapa saja sesuai kondisi toko.",
            "Pelunasan hutang dicatat dari menu Laporan agar riwayat pembayaran jelas.",
        ),
        icon = Icons.Outlined.BarChart,
    ),
    HomeGuideItem(
        title = "Laporan dan Riwayat",
        description = "Dipakai untuk melihat pembukuan harian, mingguan, dan bulanan.",
        steps = listOf(
            "Buka Laporan untuk melihat penjualan, profit, item terjual, dan stok.",
            "Riwayat menampilkan daftar transaksi beserta detail barangnya.",
            "Export PDF tersedia untuk periode hari ini, 7 hari, atau bulan berjalan.",
            "PDF berisi transaksi, hutang, pembayaran hutang, dan laporan stok.",
        ),
        icon = Icons.Outlined.BarChart,
    ),
    HomeGuideItem(
        title = "Printer Struk",
        description = "Printer thermal diatur dari menu Pengaturan.",
        steps = listOf(
            "Pairing printer lewat Bluetooth HP terlebih dahulu.",
            "Buka Pengaturan, pilih printer, lalu sambungkan.",
            "Jika transaksi berhasil, tombol Print akan mencetak struk ke printer yang aktif.",
            "Jika printer tidak mencetak, cek baterai, kertas, pairing Bluetooth, dan pilihan printer.",
        ),
        icon = Icons.Outlined.Settings,
    ),
    HomeGuideItem(
        title = "Backup dan Restore",
        description = "Fitur ini penting karena data disimpan offline di perangkat.",
        steps = listOf(
            "Backup Data membuat file ZIP di folder Download/KasirKoperasi.",
            "File backup berisi database, foto produk, logo toko, dan pengaturan.",
            "Restore Data akan mengganti data aplikasi dengan isi file backup.",
            "Lakukan backup rutin, terutama sebelum servis HP atau pindah perangkat.",
        ),
        icon = Icons.Outlined.Settings,
    ),
    HomeGuideItem(
        title = "Tips Operasional",
        description = "Kebiasaan kecil ini mengurangi risiko salah input dan kehilangan data.",
        steps = listOf(
            "Cek stok menipis dari Beranda sebelum toko ramai.",
            "Gunakan foto produk agar kasir lebih mudah mengenali barang.",
            "Pastikan nama pembeli hutang selalu konsisten.",
            "Jangan hapus folder foto aplikasi secara manual dari file manager.",
        ),
        icon = Icons.Outlined.Info,
    ),
)

@Composable
private fun HomeInfoSheet(
    onDismiss: () -> Unit,
) {
    val panelHeightFraction = 0.88f

    ModalOverlayWindow(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f)),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(1f - panelHeightFraction)
                    .dismissPanelOnTap(onDismiss),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(panelHeightFraction),
                color = CreamBackground,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HomePanelHandle(onDismiss = onDismiss)

                    Text(
                        text = "Panduan KasirKoperasi",
                        modifier = Modifier.fillMaxWidth(),
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Ringkasan cara memakai fitur utama aplikasi untuk operasional koperasi.",
                        modifier = Modifier.fillMaxWidth(),
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(
                            items = homeGuideItems(),
                            key = { it.title },
                        ) { item ->
                            HomeGuideCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGuideCard(
    item: HomeGuideItem,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepGreen.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = DeepGreen,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = item.title,
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.description,
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(DeepGreen.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                color = DeepGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = step,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF303A34),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
    ModalOverlayWindow(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .dismissPanelOnTap(onDismiss),
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
    val context = LocalContext.current
    val productBitmap = remember(product.imageUri) {
        product.imageUri
            ?.takeIf { it.isNotBlank() }
            ?.let { ProductImageStore.loadBitmap(context = context, imageUri = it, targetSize = 120) }
    }

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
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoftGray),
                contentAlignment = Alignment.Center,
            ) {
                if (productBitmap != null) {
                    Image(
                        bitmap = productBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = product.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
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
                title = "Produk",
                icon = Icons.Outlined.Inventory2,
                accentColor = DeepGreen,
                onClick = { onRouteSelected(AppRoute.Product.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Scan\nBarcode",
                icon = Icons.Outlined.QrCodeScanner,
                accentColor = Color(0xFF0E7490),
                onClick = onScanBarcode,
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Pengeluaran",
                icon = Icons.Outlined.AttachMoney,
                accentColor = Color(0xFFB45309),
                onClick = { onRouteSelected(AppRoute.Expense.route) },
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
                accentColor = Color(0xFF7C3AED),
                onClick = { onRouteSelected(AppRoute.History.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Laporan",
                icon = Icons.Outlined.BarChart,
                accentColor = Color(0xFF15803D),
                onClick = { onRouteSelected(AppRoute.Report.route) },
                modifier = Modifier.weight(1f),
            )
            MenuCard(
                title = "Setting",
                icon = Icons.Outlined.Settings,
                accentColor = Color(0xFF475569),
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
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(136.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(accentColor.copy(alpha = 0.045f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.08f)),
                ) {
                    Box(
                        modifier = Modifier.size(58.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(31.dp),
                            tint = accentColor,
                        )
                    }
                }

                Text(
                    text = title,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.88f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = accentColor,
                    )
                }
            }
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
private fun ReturnProductButton(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.24f)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = DeepGreen,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Retur Barang",
                color = DeepGreen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ReturnProductSheet(
    uiState: HomeReturnUiState,
    onTransactionSelected: (SalesTransaction) -> Unit,
    onTransactionListRequested: () -> Unit,
    onItemSelected: (SalesTransactionItem) -> Unit,
    onProcessReturn: () -> Unit,
    onDismiss: () -> Unit,
) {
    val successMessage = uiState.successMessage

    ModalOverlayWindow(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .dismissPanelOnTap(onDismiss),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .then(if (successMessage != null) Modifier.blur(5.dp) else Modifier)
                    .clickable { },
                color = CreamBackground,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HomePanelHandle(onDismiss = onDismiss)

                    ReturnSheetHeader(
                        showBackButton = uiState.selectedTransaction != null,
                        onBack = onTransactionListRequested,
                    )

                    Text(
                        text = if (uiState.selectedTransaction == null) {
                            "Pilih riwayat transaksi 7 hari terakhir."
                        } else {
                            "Pilih barang yang akan diretur dari transaksi ini."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )

                    uiState.errorMessage?.let { message ->
                        ReturnMessageCard(message = message)
                    }

                    if (uiState.selectedTransaction == null) {
                        ReturnTransactionList(
                            uiState = uiState,
                            onTransactionSelected = onTransactionSelected,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        ReturnItemList(
                            uiState = uiState,
                            onTransactionListRequested = onTransactionListRequested,
                            onItemSelected = onItemSelected,
                            onProcessReturn = onProcessReturn,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            successMessage?.let { message ->
                ReturnSuccessDialog(
                    message = message,
                    item = uiState.selectedReturnItem,
                    returnedQuantity = uiState.returnedQuantity,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun ReturnSheetHeader(
    showBackButton: Boolean,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showBackButton) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onBack() },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.14f)),
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali ke daftar transaksi",
                        modifier = Modifier.size(22.dp),
                        tint = DeepGreen,
                    )
                }
            }
        }

        Text(
            text = "Retur Barang",
            color = DeepGreen,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReturnTransactionList(
    uiState: HomeReturnUiState,
    onTransactionSelected: (SalesTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Pilih Riwayat Transaksi",
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        when {
            uiState.isLoading -> {
                ReturnMessageCard(message = "Memuat riwayat transaksi...")
            }

            uiState.transactions.isEmpty() -> {
                ReturnMessageCard(message = "Belum ada transaksi dalam 7 hari terakhir.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(
                        items = uiState.transactions,
                        key = { it.id },
                    ) { transaction ->
                        ReturnTransactionCard(
                            transaction = transaction,
                            onClick = { onTransactionSelected(transaction) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnTransactionCard(
    transaction: SalesTransaction,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepGreen.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = DeepGreen,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = transaction.transactionNumber,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = transaction.buyerName.ifBlank { "Pembeli umum" },
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = transaction.createdAtMillis.toHomeDateTime(),
                    color = MutedText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = transaction.totalAmount.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "${transaction.itemCount} item",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ReturnItemList(
    uiState: HomeReturnUiState,
    onTransactionListRequested: () -> Unit,
    onItemSelected: (SalesTransactionItem) -> Unit,
    onProcessReturn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        uiState.selectedTransaction?.let { transaction ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.16f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = transaction.transactionNumber,
                            color = Color(0xFF17221B),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${transaction.createdAtMillis.toHomeDateTime()} - ${transaction.totalAmount.toRupiah()}",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DeepGreen.copy(alpha = 0.10f),
                        modifier = Modifier.clickable { onTransactionListRequested() },
                    ) {
                        Text(
                            text = "Ganti",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = DeepGreen,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                ReturnMessageCard(message = "Memuat daftar barang...")
            }

            uiState.selectedItems.isEmpty() -> {
                ReturnMessageCard(message = "Transaksi ini tidak memiliki item barang.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(
                        items = uiState.selectedItems,
                        key = { it.id },
                    ) { item ->
                        ReturnItemCard(
                            item = item,
                            isSelected = uiState.selectedReturnItem?.id == item.id,
                            onClick = { onItemSelected(item) },
                        )
                    }
                }

                uiState.selectedReturnItem?.let { selectedItem ->
                    ReturnConfirmationCard(
                        item = selectedItem,
                        returnedQuantity = uiState.returnedQuantity,
                        remainingQuantity = uiState.remainingReturnQuantity,
                        isProcessing = uiState.isProcessingReturn,
                        onProcessReturn = onProcessReturn,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReturnSuccessDialog(
    message: String,
    item: SalesTransactionItem?,
    returnedQuantity: Int,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.38f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
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
                        .size(72.dp)
                        .background(DeepGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = DeepGreen,
                    )
                }

                Text(
                    text = "Retur Berhasil",
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                item?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFEAF3E8),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.16f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(
                                    text = it.productName,
                                    color = Color(0xFF17221B),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Stok sudah dikembalikan dan laporan dikoreksi",
                                    color = MutedText,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                text = "$returnedQuantity ${it.unit}",
                                color = DeepGreen,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "Selesai",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReturnConfirmationCard(
    item: SalesTransactionItem,
    returnedQuantity: Int,
    remainingQuantity: Int,
    isProcessing: Boolean,
    onProcessReturn: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Konfirmasi Retur",
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.productName,
                color = DeepGreen,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Terjual ${item.quantity} ${item.unit}, sudah retur $returnedQuantity ${item.unit}, sisa bisa retur $remainingQuantity ${item.unit}.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFEAF3E8),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DeepGreen.copy(alpha = 0.14f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Barang yang akan dikembalikan",
                            color = MutedText,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = "Mengikuti jumlah barang pada transaksi",
                            color = MutedText,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        text = "$remainingQuantity ${item.unit}",
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Estimasi nilai retur",
                        color = MutedText,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = (remainingQuantity.toLong() * item.sellingPrice).toRupiah(),
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = onProcessReturn,
                    enabled = remainingQuantity > 0 && !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (isProcessing) "Memproses..." else "Proses Retur",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReturnItemCard(
    item: SalesTransactionItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) DeepGreen.copy(alpha = 0.45f) else LineSoft,
        label = "returnItemBorder",
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF3F8F1) else Color.White,
        label = "returnItemBackground",
    )
    val leadingBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) DeepGreen else SoftGray,
        label = "returnItemLeadingBackground",
    )
    val leadingTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else DeepGreen,
        label = "returnItemLeadingText",
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 1.dp,
        label = "returnItemElevation",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(leadingBackgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.productName.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    color = leadingTextColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.productName,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.quantity} ${item.unit} - ${item.sellingPrice.toRupiah()}",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
                Text(
                    text = if (isSelected) "Barang dipilih untuk retur" else "Tap untuk pilih barang retur",
                    color = if (isSelected) DeepGreen else MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.subtotal.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = DeepGreen,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(18.dp),
                            tint = Color.White,
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MutedText.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReturnMessageCard(
    message: String,
    isSuccess: Boolean = false,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) DeepGreen.copy(alpha = 0.10f) else Color.White,
        ),
        border = BorderStroke(1.dp, if (isSuccess) DeepGreen.copy(alpha = 0.22f) else LineSoft),
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = if (isSuccess) DeepGreen else MutedText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSuccess) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
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

private fun Long.toHomeDateTime(): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date(this))
}

private fun calculateProfitPercentage(
    sales: Long,
    profit: Long,
): String {
    if (sales <= 0L) return "0%"

    val percentage = (profit * 100) / sales
    return "$percentage%"
}
