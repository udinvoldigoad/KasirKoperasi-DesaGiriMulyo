package com.kasirkoperasi.app.feature.stock.viewmodel

import androidx.lifecycle.ViewModel
import com.kasirkoperasi.app.feature.stock.state.StockUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StockViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()
}
