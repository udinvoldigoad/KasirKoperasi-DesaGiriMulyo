package com.kasirkoperasi.app.feature.scanner.viewmodel

import androidx.lifecycle.ViewModel
import com.kasirkoperasi.app.feature.scanner.state.ScannerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
}
