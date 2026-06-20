package com.kasirkoperasi.app.feature.product.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kasirkoperasi.app.core.image.ProductImageStore
import com.kasirkoperasi.app.core.navigation.AppRoute
import com.kasirkoperasi.app.core.ui.KasirBottomBar
import com.kasirkoperasi.app.core.ui.KoperasiLogo
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ProductCategory
import com.kasirkoperasi.app.feature.product.state.ProductUiState
import com.kasirkoperasi.app.ui.theme.CreamBackground
import com.kasirkoperasi.app.ui.theme.DeepGreen
import com.kasirkoperasi.app.ui.theme.FreshMint
import com.kasirkoperasi.app.ui.theme.LineSoft
import com.kasirkoperasi.app.ui.theme.MutedText
import com.kasirkoperasi.app.ui.theme.SoftGray
import kotlinx.coroutines.delay

@Composable
fun ProductScreen(
    uiState: ProductUiState,
    onSaveProduct: (
        name: String,
        category: String,
        barcode: String,
        unit: String,
        purchasePrice: String,
        sellingPrice: String,
        stockQuantity: String,
        imageUri: String,
    ) -> Unit,
    onUpdateProduct: (
        product: Product,
        name: String,
        category: String,
        purchasePrice: String,
        sellingPrice: String,
        stockInQuantity: String,
        imageUri: String,
    ) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onClearMessage: () -> Unit,
    onImageDeletionHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
    selectedRoute: String = AppRoute.Product.route,
    onRouteSelected: (String) -> Unit = {},
    storeLogoUri: String? = null,
) {
    var isAddingProduct by rememberSaveable { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(uiState.successMessage) {
        if (editingProduct != null && uiState.successMessage != null) {
            editingProduct = null
        }
    }

    LaunchedEffect(uiState.imageUriToDelete) {
        uiState.imageUriToDelete?.let { imageUri ->
            ProductImageStore.deleteImage(imageUri)
            onImageDeletionHandled()
        }
    }

    if (isAddingProduct) {
        ProductFormScreen(
            uiState = uiState,
            onBackClick = {
                isAddingProduct = false
                onClearMessage()
            },
            onSaveProduct = onSaveProduct,
            onClearMessage = onClearMessage,
            onSaved = {
                isAddingProduct = false
            },
            modifier = modifier,
        )
    } else {
        ProductListScreen(
            uiState = uiState,
            selectedRoute = selectedRoute,
            storeLogoUri = storeLogoUri,
            onAddClick = {
                isAddingProduct = true
                onClearMessage()
            },
            onProductClick = { product ->
                editingProduct = product
                onClearMessage()
            },
            onClearMessage = onClearMessage,
            onRouteSelected = onRouteSelected,
            modifier = modifier,
        )
    }

    editingProduct?.let { product ->
        ProductEditSheet(
            product = product,
            isSaving = uiState.isSaving,
            errorMessage = uiState.errorMessage,
            onDismiss = {
                editingProduct = null
                onClearMessage()
            },
            onSave = onUpdateProduct,
            onDelete = onDeleteProduct,
            onClearMessage = onClearMessage,
        )
    }
}

@Composable
private fun ProductListScreen(
    uiState: ProductUiState,
    selectedRoute: String,
    storeLogoUri: String?,
    onAddClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onClearMessage: () -> Unit,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Semua") }
    var selectedStockSort by rememberSaveable { mutableStateOf(ProductStockSort.Default) }
    var isStockFilterPanelVisible by rememberSaveable { mutableStateOf(false) }

    val filteredProducts by remember(uiState.products, searchQuery, selectedCategory, selectedStockSort) {
        derivedStateOf {
            val filtered = uiState.products.filter { product ->
                val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) ||
                    product.barcode.orEmpty().contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == ProductCategory.ALL ||
                    ProductCategory.normalize(product.category) == selectedCategory

                matchesSearch && matchesCategory
            }

            when (selectedStockSort) {
                ProductStockSort.Default -> filtered
                ProductStockSort.LowestStock -> filtered.sortedWith(
                    compareBy<Product> { it.stockQuantity }
                        .thenBy { it.name.lowercase() },
                )
                ProductStockSort.HighestStock -> filtered.sortedWith(
                    compareByDescending<Product> { it.stockQuantity }
                        .thenBy { it.name.lowercase() },
                )
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(2200)
            onClearMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CreamBackground,
            topBar = {
                ProductTopBar(
                    title = "Barang",
                    leftContent = { KoperasiLogo(logoUri = storeLogoUri) },
                    rightContent = null,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SearchAndFilterRow(
                        searchQuery = searchQuery,
                        onSearchChange = {
                            searchQuery = it
                            onClearMessage()
                        },
                        onSearchClear = {
                            searchQuery = ""
                            onClearMessage()
                        },
                        isStockSortActive = selectedStockSort != ProductStockSort.Default,
                        onFilterClick = {
                            isStockFilterPanelVisible = true
                            onClearMessage()
                        },
                    )
                }

                item {
                    CategoryChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
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

                if (uiState.isLoading) {
                    item {
                        LoadingCard()
                    }
                } else if (filteredProducts.isEmpty()) {
                    item {
                        EmptyProductCard()
                    }
                }

                items(
                    items = filteredProducts,
                    key = { it.id },
                ) { product ->
                    ProductItemCard(
                        product = product,
                        onClick = { onProductClick(product) },
                    )
                }
            }
        }

        uiState.successMessage?.let { message ->
            FloatingSuccessMessage(
                message = message,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 16.dp, end = 16.dp),
            )
        }

        if (isStockFilterPanelVisible) {
            ProductStockFilterPanel(
                selectedSort = selectedStockSort,
                onDismiss = { isStockFilterPanelVisible = false },
                onSortSelected = { sort ->
                    selectedStockSort = sort
                    isStockFilterPanelVisible = false
                    onClearMessage()
                },
            )
        }
    }
}

@Composable
private fun ProductFormScreen(
    uiState: ProductUiState,
    onBackClick: () -> Unit,
    onSaveProduct: (
        name: String,
        category: String,
        barcode: String,
        unit: String,
        purchasePrice: String,
        sellingPrice: String,
        stockQuantity: String,
        imageUri: String,
    ) -> Unit,
    onClearMessage: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ProductCategory.DEFAULT) }
    var barcode by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("pcs") }
    var purchasePrice by rememberSaveable { mutableStateOf("") }
    var sellingPrice by rememberSaveable { mutableStateOf("") }
    var stockQuantity by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf("") }
    var isImageSourcePanelVisible by remember { mutableStateOf(false) }
    val imagePickerActions = rememberProductImagePicker(
        onImageSaved = { imageUri = it },
    )

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            name = ""
            category = ProductCategory.DEFAULT
            barcode = ""
            unit = "pcs"
            purchasePrice = ""
            sellingPrice = ""
            stockQuantity = ""
            imageUri = ""
            onSaved()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CreamBackground,
            topBar = {
                ProductTopBar(
                    title = "Tambah Produk Baru",
                    leftContent = {
                        BackLineIcon()
                    },
                    rightContent = null,
                    onLeftClick = onBackClick,
                )
            },
            bottomBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                ) {
                    Button(
                        onClick = {
                            onSaveProduct(
                                name,
                                category,
                                barcode,
                                unit,
                                purchasePrice,
                                sellingPrice,
                                stockQuantity,
                                imageUri,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(18.dp)
                            .height(58.dp),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepGreen,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = if (uiState.isSaving) "Menyimpan..." else "Simpan Produk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    top = 28.dp,
                    end = 18.dp,
                    bottom = 104.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    ProductImagePlaceholder(
                        imageUri = imageUri,
                        onClick = { isImageSourcePanelVisible = true },
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

            item {
                KasirTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onClearMessage()
                    },
                    label = "Nama Produk*",
                    leading = { BagLineIcon() },
                )
            }

            item {
                ProductCategorySelector(
                    selectedCategory = category,
                    onCategorySelected = {
                        category = it
                        onClearMessage()
                    },
                )
            }

            item {
                KasirTextField(
                    value = barcode,
                    onValueChange = {
                        barcode = it
                        onClearMessage()
                    },
                    label = "Barcode (Opsional)",
                    leading = { BarcodeLineIcon() },
                    keyboardType = KeyboardType.Number,
                )
            }

            item {
                KasirTextField(
                    value = purchasePrice,
                    onValueChange = {
                        purchasePrice = it.toGroupedNumberInput()
                        onClearMessage()
                    },
                    label = "Harga Beli",
                    leading = { MoneyLineIcon() },
                    keyboardType = KeyboardType.Number,
                )
            }

            item {
                KasirTextField(
                    value = sellingPrice,
                    onValueChange = {
                        sellingPrice = it.toGroupedNumberInput()
                        onClearMessage()
                    },
                    label = "Harga Jual*",
                    leading = { MoneyLineIcon() },
                    keyboardType = KeyboardType.Number,
                )
            }

            item {
                KasirTextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                        onClearMessage()
                    },
                    label = "Satuan",
                    leading = { BoxLineIcon() },
                )
            }

            item {
                StockManagementCard(
                    stockQuantity = stockQuantity,
                    onStockQuantityChange = {
                        stockQuantity = it.onlyDigits()
                        onClearMessage()
                    },
                )
            }
        }
        }

        if (isImageSourcePanelVisible) {
            ProductImageSourcePanel(
                onDismiss = { isImageSourcePanelVisible = false },
                onTakePhoto = {
                    isImageSourcePanelVisible = false
                    imagePickerActions.openCamera()
                },
                onPickFromGallery = {
                    isImageSourcePanelVisible = false
                    imagePickerActions.openGallery()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditSheet(
    product: Product,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (
        product: Product,
        name: String,
        category: String,
        purchasePrice: String,
        sellingPrice: String,
        stockInQuantity: String,
        imageUri: String,
    ) -> Unit,
    onDelete: (Product) -> Unit,
    onClearMessage: () -> Unit,
) {
    var name by rememberSaveable(product.id) { mutableStateOf(product.name) }
    var category by rememberSaveable(product.id) { mutableStateOf(ProductCategory.normalize(product.category)) }
    var purchasePrice by rememberSaveable(product.id) {
        mutableStateOf(product.purchasePrice.takeIf { it > 0L }?.toGroupedNumber().orEmpty())
    }
    var sellingPrice by rememberSaveable(product.id) {
        mutableStateOf(product.sellingPrice.takeIf { it > 0L }?.toGroupedNumber().orEmpty())
    }
    var stockInQuantity by rememberSaveable(product.id) { mutableStateOf("") }
    var imageUri by rememberSaveable(product.id) { mutableStateOf(product.imageUri.orEmpty()) }
    var showDeleteConfirm by rememberSaveable(product.id) { mutableStateOf(false) }
    var isImageSourcePanelVisible by remember { mutableStateOf(false) }
    val imagePickerActions = rememberProductImagePicker(
        onImageSaved = { imageUri = it },
    )

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
                    .imePadding()
                    .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProductPanelHandle(onDismiss = onDismiss)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = "Edit Barang",
                            modifier = Modifier.fillMaxWidth(),
                            color = DeepGreen,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    item {
                        ProductImagePlaceholder(
                            imageUri = imageUri,
                            onClick = { isImageSourcePanelVisible = true },
                        )
                    }

                    errorMessage?.let { message ->
                        item {
                            MessageCard(
                                message = message,
                                isError = true,
                            )
                        }
                    }

                    item {
                        LabeledKasirTextField(
                            title = "Nama Produk",
                            value = name,
                            onValueChange = {
                                name = it
                                onClearMessage()
                            },
                            label = "Nama Produk*",
                            leading = { BagLineIcon() },
                        )
                    }

                    item {
                        ProductCategorySelector(
                            selectedCategory = category,
                            onCategorySelected = {
                                category = it
                                onClearMessage()
                            },
                        )
                    }

                    item {
                        LabeledKasirTextField(
                            title = "Harga Beli",
                            value = purchasePrice,
                            onValueChange = {
                                purchasePrice = it.toGroupedNumberInput()
                                onClearMessage()
                            },
                            label = "Harga Beli",
                            leading = { MoneyLineIcon() },
                            keyboardType = KeyboardType.Number,
                        )
                    }

                    item {
                        LabeledKasirTextField(
                            title = "Harga Jual",
                            value = sellingPrice,
                            onValueChange = {
                                sellingPrice = it.toGroupedNumberInput()
                                onClearMessage()
                            },
                            label = "Harga Jual*",
                            leading = { MoneyLineIcon() },
                            keyboardType = KeyboardType.Number,
                        )
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, LineSoft),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Stok Saat Ini",
                                        color = MutedText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "${product.stockQuantity} ${product.unit}",
                                        color = DeepGreen,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                KasirTextField(
                                    value = stockInQuantity,
                                    onValueChange = {
                                        stockInQuantity = it.onlyDigits()
                                        onClearMessage()
                                    },
                                    label = "Stok Masuk",
                                    leading = { BoxLineIcon() },
                                    keyboardType = KeyboardType.Number,
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                onSave(
                                    product,
                                    name,
                                    category,
                                    purchasePrice,
                                    sellingPrice,
                                    stockInQuantity,
                                    imageUri,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepGreen,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                text = if (isSaving) "Menyimpan..." else "Simpan Perubahan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    item {
                        if (showDeleteConfirm) {
                            DeleteConfirmationCard(
                                isSaving = isSaving,
                                onCancel = { showDeleteConfirm = false },
                                onDelete = { onDelete(product) },
                            )
                        } else {
                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                ),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(
                                    text = "Hapus Barang",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isImageSourcePanelVisible) {
            ProductImageSourcePanel(
                onDismiss = { isImageSourcePanelVisible = false },
                onTakePhoto = {
                    isImageSourcePanelVisible = false
                    imagePickerActions.openCamera()
                },
                onPickFromGallery = {
                    isImageSourcePanelVisible = false
                    imagePickerActions.openGallery()
                },
            )
        }
    }
}

@Composable
private fun ProductTopBar(
    title: String,
    leftContent: @Composable () -> Unit,
    rightContent: (@Composable () -> Unit)?,
    onLeftClick: () -> Unit = {},
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onLeftClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    leftContent()
                }

                Text(
                    text = title,
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
                    rightContent?.invoke()
                }
            }
        }
    }
}

private data class ProductImagePickerActions(
    val openCamera: () -> Unit,
    val openGallery: () -> Unit,
)

@Composable
private fun rememberProductImagePicker(
    onImageSaved: (String) -> Unit,
): ProductImagePickerActions {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { selectedUri ->
        selectedUri?.let { uri ->
            runCatching {
                ProductImageStore.persistImage(context, uri)
            }.onSuccess(onImageSaved)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { isSuccess ->
        if (isSuccess) {
            pendingCameraUri?.let { uri ->
                runCatching {
                    ProductImageStore.persistImage(context, uri)
                }.onSuccess(onImageSaved)
            }
        }
        pendingCameraUri = null
    }

    return remember(context, galleryLauncher, cameraLauncher) {
        ProductImagePickerActions(
            openCamera = {
                val uri = ProductImageStore.createCameraImageUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            },
            openGallery = {
                galleryLauncher.launch("image/*")
            },
        )
    }
}

@Composable
private fun ProductImageSourcePanel(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
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
                .fillMaxHeight(0.38f)
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
                ProductPanelHandle(onDismiss = onDismiss)

                Text(
                    text = "Foto Produk",
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                ImageSourceOption(
                    title = "Ambil Foto",
                    description = "Gunakan kamera perangkat.",
                    icon = Icons.Outlined.CameraAlt,
                    onClick = onTakePhoto,
                )

                ImageSourceOption(
                    title = "Pilih dari Galeri",
                    description = "Ambil gambar yang sudah ada.",
                    icon = Icons.Outlined.PhotoLibrary,
                    onClick = onPickFromGallery,
                )
            }
        }
    }
}

@Composable
private fun ImageSourceOption(
    title: String,
    description: String,
    icon: ImageVector,
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
                    imageVector = icon,
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
private fun SearchAndFilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    isStockSortActive: Boolean,
    onFilterClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KasirTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            label = "Cari produk...",
            leading = { SearchLineIcon() },
            trailing = if (searchQuery.isNotBlank()) {
                {
                    IconButton(
                        onClick = onSearchClear,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Hapus pencarian",
                            modifier = Modifier.size(20.dp),
                            tint = MutedText,
                        )
                    }
                }
            } else {
                null
            },
        )

        SquareIconButton(
            onClick = onFilterClick,
            active = isStockSortActive,
        ) {
            FilterLineIcon()
        }
    }
}

@Composable
private fun ProductStockFilterPanel(
    selectedSort: ProductStockSort,
    onDismiss: () -> Unit,
    onSortSelected: (ProductStockSort) -> Unit,
) {
    val panelHeight = if (selectedSort == ProductStockSort.Default) 0.46f else 0.56f

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
                .fillMaxHeight(panelHeight)
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
                ProductPanelHandle(onDismiss = onDismiss)

                Text(
                    text = "Filter Stok",
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Urutkan daftar barang berdasarkan jumlah stok.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        StockSortOption(
                            title = "Stok Terendah",
                            description = "Barang dengan stok paling sedikit tampil di atas.",
                            selected = selectedSort == ProductStockSort.LowestStock,
                            onClick = { onSortSelected(ProductStockSort.LowestStock) },
                        )
                    }

                    item {
                        StockSortOption(
                            title = "Stok Tertinggi",
                            description = "Barang dengan stok paling banyak tampil di atas.",
                            selected = selectedSort == ProductStockSort.HighestStock,
                            onClick = { onSortSelected(ProductStockSort.HighestStock) },
                        )
                    }

                    if (selectedSort != ProductStockSort.Default) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            ResetFilterOption(
                                onClick = { onSortSelected(ProductStockSort.Default) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetFilterOption(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Text(
            text = "Reset Filter",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StockSortOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (selected) DeepGreen.copy(alpha = 0.24f) else LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 1.dp),
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
                    .background(if (selected) FreshMint else SoftGray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = DeepGreen,
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
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ProductCategory.filterOptions) { category ->
            val selected = category == selectedCategory
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onCategorySelected(category) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) FreshMint else Color.White,
                border = BorderStroke(1.dp, if (selected) FreshMint else LineSoft),
                shadowElevation = if (selected) 0.dp else 2.dp,
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    color = if (selected) DeepGreen else Color(0xFF303A34),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ProductCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Kategori Barang",
                color = DeepGreen,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ProductCategory.options) { category ->
                    val selected = category == selectedCategory
                    val interactionSource = remember { MutableInteractionSource() }
                    Surface(
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { onCategorySelected(category) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) FreshMint else SoftGray,
                        border = BorderStroke(1.dp, if (selected) FreshMint else LineSoft),
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            color = if (selected) DeepGreen else Color(0xFF303A34),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPanelHandle(
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
private fun DeleteConfirmationCard(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Hapus barang ini?",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Barang akan disembunyikan dari daftar, tetapi data lama tetap aman untuk transaksi dan laporan nanti.",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DeepGreen,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = "Batal", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (isSaving) "Menghapus..." else "Ya, Hapus",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledKasirTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leading: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            color = DeepGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        KasirTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            leading = leading,
            keyboardType = keyboardType,
        )
    }
}

@Composable
private fun ProductImagePlaceholder(
    imageUri: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val bitmap = rememberProductBitmap(imageUri)
    val clickModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(142.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .background(SoftGray, RoundedCornerShape(22.dp))
                    .border(
                        width = 2.dp,
                        color = LineSoft,
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CameraLineIcon()
                        Text(
                            text = "Tambahkan\nGambar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                        .background(DeepGreen, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    PencilLineIcon()
                }
            }
        }

        Text(
            text = if (onClick == null) "Gambar Produk (Opsional)" else "Ketuk untuk pilih gambar",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun StockManagementCard(
    stockQuantity: String,
    onStockQuantityChange: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Manajemen Stok",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Tanpa Stok",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                TogglePill()
                Text(
                    text = "Dengan Stok",
                    color = DeepGreen,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            KasirTextField(
                value = stockQuantity,
                onValueChange = onStockQuantityChange,
                label = "Stok (Opsional)",
                leading = { BoxLineIcon() },
                keyboardType = KeyboardType.Number,
            )
        }
    }
}

@Composable
private fun TogglePill() {
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 30.dp)
            .background(FreshMint, RoundedCornerShape(50)),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(24.dp)
                .background(DeepGreen, CircleShape),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KasirTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        placeholder = {
            Text(
                text = label,
                color = MutedText,
            )
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
private fun SquareIconButton(
    onClick: () -> Unit,
    active: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(54.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (active) FreshMint else SoftGray,
        border = BorderStroke(1.dp, if (active) DeepGreen.copy(alpha = 0.2f) else LineSoft),
        shadowElevation = if (active) 0.dp else 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        FreshMint
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        DeepGreen
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
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
private fun FloatingSuccessMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, FreshMint),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(FreshMint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DeepGreen,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Berhasil",
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
private fun LoadingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = DeepGreen,
            )
            Text(text = "Memuat data barang...")
        }
    }
}

@Composable
private fun EmptyProductCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LineSoft),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Belum ada barang",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tekan tombol + untuk menambahkan barang pertama.",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ProductItemCard(
    product: Product,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0x11000000)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductThumbnail(
                productName = product.name,
                imageUri = product.imageUri,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = product.sellingPrice.toRupiah(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Stok",
                    color = MutedText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${product.stockQuantity} ${product.unit}",
                    color = DeepGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ProductThumbnail(
    productName: String,
    imageUri: String?,
) {
    val bitmap = rememberProductBitmap(imageUri)

    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SoftGray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DeepGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = productName.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SearchLineIcon() {
    KasirIcon(Icons.Outlined.Search)
}

@Composable
private fun FilterLineIcon() {
    KasirIcon(Icons.Outlined.FilterList)
}

@Composable
private fun BagLineIcon() {
    KasirIcon(Icons.Outlined.ShoppingBag)
}

@Composable
private fun BarcodeLineIcon() {
    KasirIcon(Icons.Outlined.QrCodeScanner)
}

@Composable
private fun MoneyLineIcon() {
    Text(
        text = "Rp",
        color = DeepGreen,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun BoxLineIcon() {
    KasirIcon(Icons.Outlined.Inventory2)
}

@Composable
private fun CameraLineIcon() {
    KasirIcon(Icons.Outlined.CameraAlt, size = 30.dp)
}

@Composable
private fun BackLineIcon() {
    KasirIcon(Icons.AutoMirrored.Outlined.ArrowBack, size = 28.dp, tint = Color(0xFF1C2A22))
}

@Composable
private fun PencilLineIcon() {
    KasirIcon(Icons.Outlined.Edit, size = 22.dp, tint = Color.White)
}

@Composable
private fun KasirIcon(
    imageVector: ImageVector,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    tint: Color = DeepGreen,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(size),
        tint = tint,
    )
}

private enum class ProductStockSort {
    Default,
    LowestStock,
    HighestStock,
}

private fun String.onlyDigits(): String {
    return filter { it.isDigit() }
}

private fun String.toGroupedNumberInput(): String {
    val digits = onlyDigits()
    if (digits.isBlank()) return ""

    return digits.toLongOrNull()?.toGroupedNumber() ?: digits
}

private fun Long.toGroupedNumber(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

private fun Long.toRupiah(): String {
    return "Rp${toGroupedNumber()}"
}
