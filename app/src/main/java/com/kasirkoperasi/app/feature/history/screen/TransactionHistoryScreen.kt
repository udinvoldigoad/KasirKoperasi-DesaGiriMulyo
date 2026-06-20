package com.kasirkoperasi.app.feature.history.screen

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.image.ProductImageStore
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.feature.history.state.TransactionHistoryRange
import com.kasirkoperasi.app.feature.history.state.TransactionHistoryUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DangerSoft
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LeafGreen
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    uiState: TransactionHistoryUiState,
    onRangeSelected: (TransactionHistoryRange) -> Unit,
    onRefresh: () -> Unit,
    onTransactionSelected: (SalesTransaction) -> Unit,
    onDismissDetail: () -> Unit,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    storeLogoUri: String? = null,
    productImageById: Map<Long, String?> = emptyMap(),
    onBackClick: (() -> Unit)? = null,
) {
    BackHandler(enabled = onBackClick != null && uiState.selectedTransaction == null) {
        onBackClick?.invoke()
    }
    BackHandler(enabled = uiState.selectedTransaction != null) {
        onDismissDetail()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (uiState.isLoading) 2.dp else 0.dp),
            containerColor = CreamBackground,
            topBar = {
                HistoryTopBar(
                    logoUri = storeLogoUri,
                    isRefreshing = uiState.isLoading,
                    onRefresh = onRefresh,
                    onBackClick = onBackClick,
                )
            },
            bottomBar = {
                KasirBottomBar(
                    selectedRoute = AppRoute.Report.route,
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
                    RangeFilterRow(
                        selectedRange = uiState.selectedRange,
                        onRangeSelected = onRangeSelected,
                    )
                }

                item {
                    HistorySummaryCard(uiState = uiState)
                }

                uiState.errorMessage?.let { message ->
                    item {
                        HistoryMessageCard(message = message)
                    }
                }

                item {
                    SectionTitle(
                        title = "Daftar Transaksi",
                        subtitle = "${uiState.transactions.size} transaksi ditemukan",
                    )
                }

                if (uiState.transactions.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyHistoryCard()
                    }
                } else {
                    items(
                        items = uiState.transactions,
                        key = { it.id },
                    ) { transaction ->
                        TransactionHistoryCard(
                            transaction = transaction,
                            onClick = { onTransactionSelected(transaction) },
                        )
                    }
                }
            }
        }

        uiState.selectedTransaction?.let { transaction ->
            TransactionDetailOverlay(
                transaction = transaction,
                items = uiState.selectedTransactionItems,
                productImageById = productImageById,
                isLoading = uiState.isDetailLoading,
                errorMessage = uiState.detailErrorMessage,
                onDismiss = onDismissDetail,
            )
        }

        if (uiState.isLoading) {
            HistoryLoadingModal(
                title = "Memperbarui riwayat",
                caption = "Transaksi ${uiState.selectedRange.description.lowercase()} sedang dimuat ulang.",
            )
        }
    }
}

@Composable
private fun HistoryTopBar(
    logoUri: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onBackClick: (() -> Unit)?,
) {
    val refreshRotation by rememberInfiniteTransition(label = "history-refresh-icon")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = LinearEasing),
            ),
            label = "history-refresh-rotation",
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
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Kembali ke laporan",
                        modifier = Modifier.size(26.dp),
                        tint = Color(0xFF303A34),
                    )
                }
            } else {
                KoperasiLogo(logoUri = logoUri)
            }

            Text(
                text = "Riwayat",
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
                    contentDescription = "Muat ulang riwayat",
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
private fun HistoryLoadingModal(
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
                .clickable { },
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
    val phase by rememberInfiniteTransition(label = "history-loading-dots")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "history-loading-dot-phase",
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
private fun RangeFilterRow(
    selectedRange: TransactionHistoryRange,
    onRangeSelected: (TransactionHistoryRange) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransactionHistoryRange.entries.forEach { range ->
            RangeFilterChip(
                text = range.label,
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RangeFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) DeepGreen else Color.White
    val contentColor = if (selected) Color.White else Color(0xFF303A34)
    val borderColor = if (selected) DeepGreen else LineSoft

    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = background,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HistorySummaryCard(
    uiState: TransactionHistoryUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 14.dp)
                    .size(92.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 18.dp)
                    .size(38.dp)
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
                            text = "Total ${uiState.selectedRange.label}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = uiState.selectedRange.description,
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Penjualan",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = uiState.totalSales.toRupiah(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = "${uiState.totalItems} item",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TransactionHistoryCard(
    transaction: SalesTransaction,
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
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = DeepGreen,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = transaction.buyerName.ifBlank { "Pembeli Umum" },
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = transaction.createdAtMillis.toDateTime(),
                        color = MutedText,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                PaymentBadge(
                    method = transaction.paymentMethod,
                )
            }

            HorizontalDivider(color = LineSoft)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransactionValue(
                    label = "Total",
                    value = transaction.totalAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
                TransactionValue(
                    label = "Profit",
                    value = transaction.totalProfit.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransactionValue(
                    label = "Dibayar",
                    value = transaction.paidAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
                TransactionValue(
                    label = "Kembalian",
                    value = transaction.changeAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = transaction.transactionNumber,
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${transaction.itemCount} barang",
                        color = LeafGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = LeafGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailOverlay(
    transaction: SalesTransaction,
    items: List<SalesTransactionItem>,
    productImageById: Map<Long, String?>,
    isLoading: Boolean,
    errorMessage: String?,
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
                .fillMaxHeight(0.88f)
                .clickable { },
            color = CreamBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                PanelHandle(onDismiss = onDismiss)
                DetailHeader(onDismiss = onDismiss)

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = DeepGreen,
                        trackColor = SoftGray,
                    )
                }

                TransactionDetailSummaryCard(transaction = transaction)

                errorMessage?.let { message ->
                    HistoryMessageCard(message = message)
                }

                SectionTitle(
                    title = "Isi Barang",
                    subtitle = "${items.sumOf { it.quantity }} item dari ${items.size} jenis barang",
                )

                if (items.isEmpty() && !isLoading) {
                    EmptyDetailItemsCard()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = items,
                            key = { it.id },
                        ) { item ->
                            TransactionDetailItemCard(
                                item = item,
                                imageUri = productImageById[item.productId],
                            )
                        }
                    }
                }
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
private fun DetailHeader(
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(FreshMint, CircleShape),
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
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Detail Transaksi",
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Rincian barang yang terjual",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(42.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Tutup detail transaksi",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF303A34),
            )
        }
    }
}

@Composable
private fun TransactionDetailSummaryCard(
    transaction: SalesTransaction,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = transaction.buyerName.ifBlank { "Pembeli Umum" },
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = transaction.createdAtMillis.toDateTime(),
                        color = MutedText,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                PaymentBadge(method = transaction.paymentMethod)
            }

            HorizontalDivider(color = LineSoft)

            Text(
                text = transaction.transactionNumber,
                color = MutedText,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransactionValue(
                    label = "Total",
                    value = transaction.totalAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
                TransactionValue(
                    label = "Dibayar",
                    value = transaction.paidAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransactionValue(
                    label = "Kembalian",
                    value = transaction.changeAmount.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
                TransactionValue(
                    label = "Profit",
                    value = transaction.totalProfit.toRupiah(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailItemCard(
    item: SalesTransactionItem,
    imageUri: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductPhotoThumbnail(imageUri = imageUri)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.productName,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.quantity} ${item.unit} x ${item.sellingPrice.toRupiah()}",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Profit ${item.profit.toRupiah()}",
                    color = LeafGreen,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.subtotal.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = item.category,
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProductPhotoThumbnail(
    imageUri: String?,
) {
    val bitmap = rememberProductBitmap(imageUri)
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(shape)
            .background(SoftGray, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto barang",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
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
            ?.let { ProductImageStore.loadBitmap(context = context, imageUri = it, targetSize = 160) }
    }
}

@Composable
private fun EmptyDetailItemsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Text(
            text = "Isi barang untuk transaksi ini belum ditemukan.",
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
private fun PaymentBadge(
    method: String,
) {
    val isQris = method.equals("QRIS", ignoreCase = true)
    val background = if (isQris) FreshMint else SoftGray
    val contentColor = DeepGreen

    Surface(
        shape = RoundedCornerShape(50),
        color = background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isQris) Icons.Outlined.CreditCard else Icons.Outlined.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = contentColor,
            )
            Text(
                text = method,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TransactionValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value,
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = DeepGreen,
            )
            Text(
                text = "Belum ada transaksi",
                color = DeepGreen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Transaksi yang berhasil disimpan akan muncul di sini.",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HistoryMessageCard(
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

private fun Long.toDateTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return formatter.format(Date(this))
}
