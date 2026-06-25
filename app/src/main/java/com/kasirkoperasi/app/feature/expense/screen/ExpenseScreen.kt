package com.kasirkoperasi.app.feature.expense.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.core.ui.MoneyInputField
import com.kasirkoperasi.app.domain.model.Expense
import com.kasirkoperasi.app.feature.expense.state.ExpenseUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun ExpenseScreen(
    uiState: ExpenseUiState,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveExpense: () -> Unit,
    onClearMessage: () -> Unit,
    onExpenseSaved: () -> Unit,
    storeLogoUri: String? = null,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onExpenseSaved()
            delay(2200)
            onClearMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            delay(2400)
            onClearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CreamBackground,
        topBar = {
            ExpenseTopBar(storeLogoUri = storeLogoUri)
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
                top = 16.dp,
                end = 16.dp,
                bottom = 112.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ExpenseSummaryCard(totalExpense = uiState.totalExpense)
            }

            uiState.successMessage?.let { message ->
                item {
                    ExpenseMessageCard(
                        message = message,
                        isError = false,
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    ExpenseMessageCard(
                        message = message,
                        isError = true,
                    )
                }
            }

            item {
                ExpenseFormCard(
                    title = uiState.title,
                    amountText = uiState.amountText,
                    note = uiState.note,
                    isSaving = uiState.isSaving,
                    onTitleChange = onTitleChange,
                    onAmountChange = onAmountChange,
                    onNoteChange = onNoteChange,
                    onSaveExpense = onSaveExpense,
                )
            }

            item {
                SectionTitle(title = "Pengeluaran Hari Ini")
            }

            if (uiState.expenses.isEmpty()) {
                item {
                    EmptyExpenseCard()
                }
            } else {
                items(
                    items = uiState.expenses,
                    key = { it.id },
                ) { expense ->
                    ExpenseRow(expense = expense)
                }
            }
        }
    }
}

@Composable
private fun ExpenseTopBar(
    storeLogoUri: String?,
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
            KoperasiLogo(logoUri = storeLogoUri)
            Text(
                text = "Pengeluaran",
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
private fun ExpenseSummaryCard(
    totalExpense: Long,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 18.dp)
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pengeluaran Hari Ini",
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = totalExpense.toRupiah(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Biaya operasional yang tercatat hari ini",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseFormCard(
    title: String,
    amountText: String,
    note: String,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveExpense: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(FreshMint, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        tint = DeepGreen,
                    )
                }
                Column {
                    Text(
                        text = "Catat Pengeluaran",
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Contoh: beli plastik, bensin, listrik, atau biaya angkut.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama pengeluaran") },
                placeholder = { Text("Contoh: Beli plastik") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = expenseTextFieldColors(),
            )

            MoneyInputField(
                value = amountText,
                onValueChange = onAmountChange,
                label = "Nominal pengeluaran",
                leadingIcon = {
                    Text(
                        text = "Rp",
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan") },
                placeholder = { Text("Opsional") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                colors = expenseTextFieldColors(),
            )

            Button(
                onClick = onSaveExpense,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (isSaving) "Menyimpan..." else "Simpan Pengeluaran",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
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
                    .background(SoftGray, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
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
                    text = expense.title,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = expense.note.ifBlank { expense.createdAtMillis.toTimeText() },
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = expense.amount.toRupiah(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ExpenseMessageCard(
    message: String,
    isError: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else FreshMint,
        border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error else DeepGreen.copy(alpha = 0.20f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.onErrorContainer else DeepGreen,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else DeepGreen,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyExpenseCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Text(
            text = "Belum ada pengeluaran hari ini.",
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
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF17221B),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun expenseTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DeepGreen,
    unfocusedBorderColor = LineSoft,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = DeepGreen,
)

private fun Long.toRupiah(): String {
    val grouped = toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "Rp$grouped"
}

private fun Long.toTimeText(): String {
    return SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID")).format(Date(this))
}
