package com.kasirkoperasi.app.feature.report.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.feature.report.state.ReportDailySalesPoint
import com.kasirkoperasi.app.feature.report.state.ReportExportRange
import com.kasirkoperasi.app.feature.report.state.ReportUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DangerSoft
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LeafGreen
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import com.kasirkoperasi.app.ui.theme.WarmAccent
import java.util.Calendar

@Composable
fun ReportScreen(
    uiState: ReportUiState,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onRefresh: () -> Unit,
    onExportPdf: (ReportExportRange) -> Unit,
    modifier: Modifier = Modifier,
    storeLogoUri: String? = null,
) {
    var isExportRangePanelVisible by remember { mutableStateOf(false) }
    val isShowingLoadingModal = uiState.isLoading || uiState.isExporting

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isShowingLoadingModal) 2.dp else 0.dp),
            containerColor = CreamBackground,
            topBar = {
                ReportTopBar(
                    logoUri = storeLogoUri,
                    isRefreshing = uiState.isLoading,
                    onRefresh = onRefresh,
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
                uiState.errorMessage?.let { message ->
                    item {
                        ReportMessageCard(message = message)
                    }
                }

                uiState.exportErrorMessage?.let { message ->
                    item {
                        ReportMessageCard(message = message)
                    }
                }

                item {
                    ReportMetricGrid(summary = uiState.monthlySummary)
                }

                item {
                    SevenDaySalesChartCard(points = uiState.sevenDaySales)
                }

                item {
                    ExportPdfButton(
                        enabled = !uiState.isExporting,
                        isExporting = uiState.isExporting,
                        onClick = { isExportRangePanelVisible = true },
                    )
                }

                item {
                    HistoryShortcutCard(onClick = onOpenHistory)
                }
            }
        }

        if (isExportRangePanelVisible) {
            ExportRangePanel(
                onDismiss = { isExportRangePanelVisible = false },
                onRangeSelected = { range ->
                    isExportRangePanelVisible = false
                    onExportPdf(range)
                },
            )
        }

        if (isShowingLoadingModal) {
            ModernLoadingModal(
                title = if (uiState.isExporting) "Membuat PDF pembukuan" else "Memperbarui laporan",
                caption = if (uiState.isExporting) {
                    "Tunggu sebentar, dokumen sedang disiapkan."
                } else {
                    "Data penjualan, profit, dan grafik sedang dihitung ulang."
                },
            )
        }
    }
}

@Composable
private fun ReportTopBar(
    logoUri: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val refreshRotation by rememberInfiniteTransition(label = "report-refresh-icon")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = LinearEasing),
            ),
            label = "report-refresh-rotation",
        )

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
                text = "Laporan",
                modifier = Modifier.weight(1f),
                color = DeepGreen,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isRefreshing) FreshMint else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Muat ulang laporan",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = if (isRefreshing) refreshRotation else 0f
                        },
                    tint = if (isRefreshing) DeepGreen else Color(0xFF303A34),
                )
            }
        }
    }
}

@Composable
private fun ModernLoadingModal(
    title: String,
    caption: String,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 180)),
        exit = fadeOut(animationSpec = tween(durationMillis = 120)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, FreshMint),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(FreshMint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingDots()
                    }

                    Text(
                        text = title,
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = caption,
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
private fun LoadingDots() {
    val phase by rememberInfiniteTransition(label = "report-refresh-dots")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "report-refresh-dot-phase",
        )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val wave = ((phase + index * 0.22f) % 1f).let { value ->
                if (value <= 0.5f) value * 2f else (1f - value) * 2f
            }
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer {
                        alpha = 0.42f + (wave * 0.58f)
                        scaleX = 0.72f + (wave * 0.32f)
                        scaleY = 0.72f + (wave * 0.32f)
                    }
                    .background(DeepGreen, CircleShape),
            )
        }
    }
}

@Composable
private fun ReportMetricGrid(
    summary: ReportSummary,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReportMetricCard(
                title = "Penjualan Bulan Ini",
                value = summary.totalSales.toRupiah(),
                caption = "Total transaksi",
                icon = Icons.Outlined.BarChart,
                iconBackground = FreshMint,
                modifier = Modifier.weight(1f),
            )
            ReportMetricCard(
                title = "Profit Bulan Ini",
                value = summary.totalProfit.toRupiah(),
                caption = "Keuntungan bersih",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                iconBackground = SoftGray,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReportMetricCard(
                title = "Item Terjual",
                value = summary.soldItemCount.toString(),
                caption = "Total kuantitas",
                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                iconBackground = WarmAccent.copy(alpha = 0.45f),
                modifier = Modifier.weight(1f),
            )
            ReportMetricCard(
                title = "Stok Menipis",
                value = summary.lowStockItemCount.toString(),
                caption = "Stok 5 atau kurang",
                icon = Icons.Outlined.Inventory2,
                iconBackground = SoftGray,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SevenDaySalesChartCard(
    points: List<ReportDailySalesPoint>,
) {
    val maxSales = points.maxOfOrNull { it.totalSales } ?: 0L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Grafik Penjualan 7 Hari",
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Total penjualan harian dari transaksi tersimpan.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FreshMint, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = DeepGreen,
                    )
                }
            }

            if (points.isEmpty()) {
                Text(
                    text = "Belum ada data penjualan untuk ditampilkan.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    points.forEach { point ->
                        SalesBar(
                            point = point,
                            maxSales = maxSales,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesBar(
    point: ReportDailySalesPoint,
    maxSales: Long,
    modifier: Modifier = Modifier,
) {
    val barHeight = if (maxSales <= 0L) {
        8f
    } else {
        maxOf(8f, (point.totalSales.toFloat() / maxSales.toFloat()) * 82f)
    }
    val barColor = if (point.totalSales > 0L) DeepGreen else LineSoft

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = point.totalSales.toShortRupiah(),
            color = DeepGreen,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(barHeight.dp)
                    .background(barColor, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = point.label,
            color = MutedText,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ExportPdfButton(
    enabled: Boolean,
    isExporting: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepGreen,
            contentColor = Color.White,
            disabledContainerColor = SoftGray,
            disabledContentColor = MutedText,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.PictureAsPdf,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (isExporting) "Membuat PDF..." else "Export PDF Pembukuan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ExportRangePanel(
    onDismiss: () -> Unit,
    onRangeSelected: (ReportExportRange) -> Unit,
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
                .fillMaxHeight(0.56f)
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
                ReportPanelHandle(onDismiss = onDismiss)

                Text(
                    text = "Pilih Periode Export",
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "PDF berisi rincian transaksi sesuai periode yang dipilih.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReportExportRange.entries.forEach { range ->
                        ExportRangeOption(
                            range = range,
                            onClick = { onRangeSelected(range) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportPanelHandle(
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
private fun ExportRangeOption(
    range: ReportExportRange,
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
                    .size(48.dp)
                    .background(FreshMint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DeepGreen,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = range.title,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = range.descriptionForUi(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = LeafGreen,
            )
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    caption: String,
    icon: ImageVector,
    iconBackground: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBackground, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DeepGreen,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
private fun HistoryShortcutCard(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(FreshMint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = DeepGreen,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Lihat transaksi hari ini, 7 hari, atau 30 hari terakhir.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = LeafGreen,
            )
        }
    }
}

@Composable
private fun ReportMessageCard(
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DangerSoft,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = Color(0xFF5F160F),
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

private fun Long.toShortRupiah(): String {
    return when {
        this >= 1_000_000L -> "Rp${this / 1_000_000L}jt"
        this >= 1_000L -> "Rp${this / 1_000L}rb"
        else -> "Rp$this"
    }
}

private fun ReportExportRange.descriptionForUi(): String {
    return when (this) {
        ReportExportRange.CurrentMonth -> {
            val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            "$description ($daysInMonth hari bulan ini)"
        }
        else -> description
    }
}
