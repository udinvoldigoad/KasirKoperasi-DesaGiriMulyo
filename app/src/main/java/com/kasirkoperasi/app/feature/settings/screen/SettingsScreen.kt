package com.kasirkoperasi.app.feature.settings.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kasirkoperasi.app.core.printer.BluetoothPrinterDevice
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.settings.DEFAULT_STORE_NAME
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.feature.settings.state.SettingsUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    selectedRoute: String,
    onRouteSelected: (String) -> Unit,
    onSaveStoreName: (String) -> Unit,
    onLogoSelected: (Uri) -> Unit,
    onImportCsvSelected: (Uri) -> Unit,
    onGenerateBarcodeSheet: () -> Unit,
    onCleanupProductImages: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreBackupSelected: (Uri) -> Unit,
    onLoadPrinters: () -> Unit,
    onPrinterSelected: (BluetoothPrinterDevice) -> Unit,
    onTestPrinter: () -> Unit,
    onPrinterPermissionDenied: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
    canGenerateBarcodeSheet: Boolean = false,
) {
    var storeName by rememberSaveable { mutableStateOf(uiState.storeName) }
    var pendingPrinterAction by rememberSaveable { mutableStateOf<PrinterPermissionAction?>(null) }
    val context = LocalContext.current
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let(onLogoSelected)
    }
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(onImportCsvSelected)
    }
    val backupRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(onRestoreBackupSelected)
    }
    val printerPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        val pendingAction = pendingPrinterAction
        pendingPrinterAction = null

        if (!isGranted) {
            onPrinterPermissionDenied()
            return@rememberLauncherForActivityResult
        }

        when (pendingAction) {
            PrinterPermissionAction.LoadPrinters -> onLoadPrinters()
            PrinterPermissionAction.TestPrint -> onTestPrinter()
            null -> Unit
        }
    }

    val runPrinterAction: (PrinterPermissionAction) -> Unit = { action ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingPrinterAction = action
            printerPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            when (action) {
                PrinterPermissionAction.LoadPrinters -> onLoadPrinters()
                PrinterPermissionAction.TestPrint -> onTestPrinter()
            }
        }
    }

    LaunchedEffect(uiState.storeName) {
        storeName = uiState.storeName
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            delay(2200)
            onClearMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CreamBackground,
            topBar = {
                SettingsTopBar(
                    logoUri = uiState.logoUri,
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
                    SettingsIntroCard()
                }

                item {
                    StoreProfileSettingsCard(
                        storeName = storeName,
                        savedStoreName = uiState.storeName,
                        logoUri = uiState.logoUri,
                        isSaving = uiState.isSaving,
                        onStoreNameChange = { storeName = it },
                        onSaveClick = { onSaveStoreName(storeName) },
                        onChangeLogoClick = {
                            logoPickerLauncher.launch("image/*")
                        },
                    )
                }

                item {
                    PrinterConnectionCard(
                        selectedPrinterName = uiState.selectedPrinterName,
                        selectedPrinterAddress = uiState.selectedPrinterAddress,
                        pairedPrinters = uiState.pairedPrinters,
                        isLoadingPrinters = uiState.isLoadingPrinters,
                        isTestingPrinter = uiState.isTestingPrinter,
                        onLoadPrintersClick = {
                            runPrinterAction(PrinterPermissionAction.LoadPrinters)
                        },
                        onPrinterSelected = onPrinterSelected,
                        onTestPrintClick = {
                            runPrinterAction(PrinterPermissionAction.TestPrint)
                        },
                    )
                }

                item {
                    DataProductSettingsCard(
                        isImporting = uiState.isImporting,
                        isCleaningImages = uiState.isCleaningImages,
                        isBackingUp = uiState.isBackingUp,
                        isRestoring = uiState.isRestoring,
                        canImport = !uiState.isSaving &&
                            !uiState.isImporting &&
                            !uiState.isCleaningImages &&
                            !uiState.isBackingUp &&
                            !uiState.isRestoring,
                        canGenerateBarcodeSheet = canGenerateBarcodeSheet,
                        onGenerateBarcodeSheetClick = onGenerateBarcodeSheet,
                        onCleanupProductImagesClick = onCleanupProductImages,
                        onBackupDataClick = onBackupData,
                        onRestoreBackupClick = {
                            backupRestoreLauncher.launch(BACKUP_MIME_TYPES)
                        },
                        onImportClick = {
                            csvImportLauncher.launch(CSV_MIME_TYPES)
                        },
                    )
                }
            }
        }

        uiState.successMessage?.let { message ->
            FloatingSettingsMessage(
                message = message,
                isError = false,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 16.dp, end = 16.dp),
            )
        }

        uiState.errorMessage?.let { message ->
            FloatingSettingsMessage(
                message = message,
                isError = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 16.dp, end = 16.dp),
            )
        }
    }
}

@Composable
private fun SettingsTopBar(
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
                text = "Pengaturan",
                modifier = Modifier.weight(1f),
                color = DeepGreen,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun SettingsIntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.White,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Pengaturan Aplikasi",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Atur identitas koperasi, printer struk, dan data produk dari satu tempat.",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StoreProfileSettingsCard(
    storeName: String,
    savedStoreName: String,
    logoUri: String?,
    isSaving: Boolean,
    onStoreNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onChangeLogoClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NumberedSectionHeader(
                number = "1",
                title = "Identitas Toko",
                subtitle = "Logo dan nama ini muncul di navbar aplikasi dan struk.",
                icon = Icons.Outlined.Storefront,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = FreshMint.copy(alpha = 0.56f),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KoperasiLogo(
                        logoUri = logoUri,
                        fallbackText = savedStoreName.firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                        size = 68.dp,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = savedStoreName.ifBlank { DEFAULT_STORE_NAME },
                            color = Color(0xFF17221B),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Pratinjau tampilan toko saat ini.",
                            color = MutedText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = storeName,
                onValueChange = onStoreNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                label = { Text("Nama toko/koperasi") },
                placeholder = { Text(DEFAULT_STORE_NAME) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepGreen,
                    unfocusedBorderColor = LineSoft,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = DeepGreen,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onChangeLogoClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LineSoft),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Ganti Logo",
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SoftGray,
                        disabledContentColor = MutedText,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (isSaving) "Menyimpan" else "Simpan",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreIdentityCard(
    storeName: String,
    logoUri: String?,
    isSaving: Boolean,
    onChangeLogoClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KoperasiLogo(
                logoUri = logoUri,
                fallbackText = storeName.firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                size = 86.dp,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = storeName.ifBlank { DEFAULT_STORE_NAME },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Identitas ini dipakai untuk navbar dan struk nanti.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Button(
                onClick = onChangeLogoClick,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DeepGreen,
                    disabledContainerColor = SoftGray,
                    disabledContentColor = MutedText,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isSaving) "Menyimpan..." else "Ubah Logo",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StoreNameCard(
    storeName: String,
    isSaving: Boolean,
    onStoreNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(
                title = "Nama Toko",
                subtitle = "Nama ini mengganti teks KasirKoperasi di navbar.",
                icon = Icons.Outlined.Storefront,
            )

            OutlinedTextField(
                value = storeName,
                onValueChange = onStoreNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true,
                placeholder = { Text(DEFAULT_STORE_NAME) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepGreen,
                    unfocusedBorderColor = LineSoft,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = DeepGreen,
                ),
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                    disabledContainerColor = SoftGray,
                    disabledContentColor = MutedText,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isSaving) "Menyimpan..." else "Simpan Nama Toko",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun PrinterConnectionCard(
    selectedPrinterName: String?,
    selectedPrinterAddress: String?,
    pairedPrinters: List<BluetoothPrinterDevice>,
    isLoadingPrinters: Boolean,
    isTestingPrinter: Boolean,
    onLoadPrintersClick: () -> Unit,
    onPrinterSelected: (BluetoothPrinterDevice) -> Unit,
    onTestPrintClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NumberedSectionHeader(
                number = "2",
                title = "Printer Struk",
                subtitle = "Pilih printer thermal yang akan dipakai saat mencetak struk.",
                icon = Icons.Outlined.Print,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (selectedPrinterName.isNullOrBlank()) SoftGray else FreshMint.copy(alpha = 0.68f),
                border = BorderStroke(
                    1.dp,
                    if (selectedPrinterName.isNullOrBlank()) LineSoft else DeepGreen.copy(alpha = 0.22f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = if (selectedPrinterName.isNullOrBlank()) {
                            "Printer belum dipilih"
                        } else {
                            "Siap cetak dengan $selectedPrinterName"
                        },
                        color = DeepGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = selectedPrinterAddress
                            ?: "Nyalakan printer, lalu pairing dari pengaturan Bluetooth HP sebelum memilih di sini.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InstructionRow(
                    number = "1",
                    text = "Pastikan printer sudah tersanding di Bluetooth HP.",
                )
                InstructionRow(
                    number = "2",
                    text = "Tekan Cari Printer, lalu pilih nama printer yang benar.",
                )
                InstructionRow(
                    number = "3",
                    text = "Tekan Coba Cetak untuk memastikan struk keluar.",
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onLoadPrintersClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !isLoadingPrinters && !isTestingPrinter,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, LineSoft),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (isLoadingPrinters) "Mencari..." else "Cari Printer",
                        fontWeight = FontWeight.Bold,
                    )
                }

                Button(
                    onClick = onTestPrintClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !selectedPrinterAddress.isNullOrBlank() && !isTestingPrinter && !isLoadingPrinters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SoftGray,
                        disabledContentColor = MutedText,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = if (isTestingPrinter) "Mencetak..." else "Coba Cetak",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (pairedPrinters.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CreamBackground,
                    border = BorderStroke(1.dp, LineSoft),
                ) {
                    Text(
                        text = "Belum ada daftar printer. Tekan Cari Printer setelah printer dipairing di HP.",
                        modifier = Modifier.padding(14.dp),
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Pilih printer:",
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    pairedPrinters.forEach { printer ->
                        PrinterDeviceRow(
                            printer = printer,
                            isSelected = printer.address.equals(selectedPrinterAddress, ignoreCase = true),
                            onClick = { onPrinterSelected(printer) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrinterDeviceRow(
    printer: BluetoothPrinterDevice,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) FreshMint else CreamBackground,
        border = BorderStroke(1.dp, if (isSelected) DeepGreen.copy(alpha = 0.26f) else LineSoft),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (isSelected) DeepGreen else SoftGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Print,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isSelected) Color.White else DeepGreen,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = printer.name,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = printer.address,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Printer dipilih",
                    modifier = Modifier.size(26.dp),
                    tint = DeepGreen,
                )
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) DeepGreen else MutedText
    val backgroundColor = if (enabled) FreshMint else SoftGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = contentColor,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    color = Color(0xFF17221B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, LineSoft),
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun DataProductSettingsCard(
    isImporting: Boolean,
    isCleaningImages: Boolean,
    isBackingUp: Boolean,
    isRestoring: Boolean,
    canImport: Boolean,
    canGenerateBarcodeSheet: Boolean,
    onGenerateBarcodeSheetClick: () -> Unit,
    onCleanupProductImagesClick: () -> Unit,
    onBackupDataClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NumberedSectionHeader(
                number = "3",
                title = "Data Produk",
                subtitle = "Gunakan bagian ini untuk data barang dan barcode rak produk.",
                icon = Icons.Outlined.FileDownload,
            )

            DataToolRow(
                title = "Generate Barcode Produk A4",
                description = "Membuat PDF A4 berisi barcode garis semua produk.",
                icon = Icons.Outlined.QrCodeScanner,
                enabled = canGenerateBarcodeSheet && !isRestoring && !isBackingUp && !isImporting && !isCleaningImages,
                isPrimary = true,
                actionLabel = if (canGenerateBarcodeSheet) "Buat PDF Barcode" else "Belum ada barcode",
                onClick = onGenerateBarcodeSheetClick,
            )

            DataToolRow(
                title = "Import Produk dari CSV",
                description = "Pakai file tabel CSV agar data barang tidak perlu diketik satu per satu.",
                icon = Icons.Outlined.FileDownload,
                enabled = canImport,
                isPrimary = true,
                actionLabel = if (isImporting) "Sedang import..." else "Pilih File CSV",
                onClick = onImportClick,
            )

            DataToolRow(
                title = "Bersihkan Foto Produk",
                description = "Hapus cache kamera dan file foto lama yang sudah tidak dipakai produk.",
                icon = Icons.Outlined.PhotoLibrary,
                enabled = !isImporting && !isCleaningImages && !isBackingUp && !isRestoring,
                isPrimary = false,
                actionLabel = if (isCleaningImages) "Membersihkan..." else "Bersihkan",
                onClick = onCleanupProductImagesClick,
            )

            DataToolRow(
                title = "Backup Data",
                description = "Simpan database, foto produk, logo, nama toko, dan printer ke file ZIP.",
                icon = Icons.Outlined.FileUpload,
                enabled = !isImporting && !isCleaningImages && !isBackingUp && !isRestoring,
                isPrimary = true,
                actionLabel = if (isBackingUp) "Membuat..." else "Buat Backup",
                onClick = onBackupDataClick,
            )

            DataToolRow(
                title = "Restore Data",
                description = "Pulihkan file backup ZIP. Data di perangkat ini akan diganti oleh isi backup.",
                icon = Icons.Outlined.Refresh,
                enabled = !isImporting && !isCleaningImages && !isBackingUp && !isRestoring,
                isPrimary = false,
                actionLabel = if (isRestoring) "Memulihkan..." else "Pilih Backup",
                onClick = onRestoreBackupClick,
            )
        }
    }
}

@Composable
private fun DataToolRow(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    isPrimary: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (enabled) FreshMint.copy(alpha = 0.58f) else SoftGray,
        border = BorderStroke(1.dp, if (enabled) DeepGreen.copy(alpha = 0.18f) else LineSoft),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(if (enabled) DeepGreen else Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) Color.White else MutedText,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF17221B),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = description,
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (isPrimary) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepGreen,
                        contentColor = Color.White,
                        disabledContainerColor = SoftGray,
                        disabledContentColor = MutedText,
                    ),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = enabled,
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, LineSoft),
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private val CSV_MIME_TYPES = arrayOf(
    "text/*",
    "text/csv",
    "application/csv",
    "application/vnd.ms-excel",
    "application/octet-stream",
)

private val BACKUP_MIME_TYPES = arrayOf(
    "application/zip",
    "application/octet-stream",
    "application/x-zip-compressed",
)

private enum class PrinterPermissionAction {
    LoadPrinters,
    TestPrint,
}

@Composable
private fun NumberedSectionHeader(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(DeepGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .background(FreshMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = DeepGreen,
            )
        }
    }
}

@Composable
private fun InstructionRow(
    number: String,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(FreshMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = DeepGreen,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = Color(0xFF17221B),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(FreshMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = DeepGreen,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = Color(0xFF17221B),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun FloatingSettingsMessage(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isError) MaterialTheme.colorScheme.errorContainer else Color.White
    val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else DeepGreen

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error else FreshMint),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor,
            )
            Text(
                text = message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
