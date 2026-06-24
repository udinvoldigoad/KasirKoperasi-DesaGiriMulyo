package com.kasirkoperasi.app.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray

@Composable
fun MoneyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    inputId: String? = null,
    activeInputId: String? = null,
    onActiveInputChange: ((String?) -> Unit)? = null,
    onKeypadVisibilityChange: (Boolean) -> Unit = {},
) {
    var localKeypadVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val activeInputChange = onActiveInputChange
    val isExternallyControlled = inputId != null && activeInputChange != null
    val isKeypadVisible = if (isExternallyControlled) {
        activeInputId == inputId
    } else {
        localKeypadVisible
    }
    val showKeypad: () -> Unit = {
        focusManager.clearFocus(force = true)
        onKeypadVisibilityChange(true)
        if (inputId != null && activeInputChange != null) {
            activeInputChange(inputId)
        } else {
            localKeypadVisible = true
        }
    }
    val hideKeypad: () -> Unit = {
        focusManager.clearFocus(force = true)
        onKeypadVisibilityChange(false)
        if (inputId != null && activeInputChange != null) {
            activeInputChange(null)
        } else {
            localKeypadVisible = false
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clickable(enabled = enabled) {
                        showKeypad()
                    },
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isKeypadVisible) DeepGreen else LineSoft,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    Text(
                        text = value.ifBlank { label },
                        modifier = Modifier.weight(1f),
                        color = if (value.isBlank()) MutedText else Color(0xFF17221B),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            trailingContent?.invoke()
        }

        if (isKeypadVisible && enabled) {
            MoneyKeyboardOverlay(
                value = value,
                label = label,
                onValueChange = onValueChange,
                onDone = hideKeypad,
            )
        }
    }
}

@Composable
private fun MoneyKeyboardOverlay(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    BackHandler(onBack = onDone)

    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissPanelOnTap(onDone),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable { },
            ) {
                MoneyKeypad(
                    value = value,
                    label = label,
                    onValueChange = onValueChange,
                    onDone = onDone,
                )
            }
        }
    }
}

@Composable
private fun MoneyKeypad(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = FreshMint,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = DeepGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = value.ifBlank { "0" },
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }

            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Hapus", "0", "000"),
            ).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { key ->
                        MoneyKeyButton(
                            label = key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val updatedValue = when (key) {
                                    "Hapus" -> value.removeLastDigitMoneyInput()
                                    "000" -> value.appendMoneyDigits("000")
                                    else -> value.appendMoneyDigits(key)
                                }
                                onValueChange(updatedValue)
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Bersihkan",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
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
private fun MoneyKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (label == "000") FreshMint else SoftGray,
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (label == "Hapus") {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Backspace,
                    contentDescription = "Hapus angka",
                    tint = DeepGreen,
                )
            } else {
                Text(
                    text = label,
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun String.appendMoneyDigits(digitsToAppend: String): String {
    val currentDigits = onlyDigits()
    val newDigits = if (currentDigits.isBlank() && digitsToAppend == "000") {
        ""
    } else {
        currentDigits + digitsToAppend
    }

    return newDigits.toMoneyInputText()
}

private fun String.removeLastDigitMoneyInput(): String {
    return onlyDigits().dropLast(1).toMoneyInputText()
}

private fun String.toMoneyInputText(): String {
    val digits = onlyDigits().trimStart('0')
    if (digits.isBlank()) return ""

    return digits.toLongOrNull()?.toGroupedNumber() ?: digits
}

private fun String.onlyDigits(): String {
    return filter { it.isDigit() }
}

private fun Long.toGroupedNumber(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}
