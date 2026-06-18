package com.kasirkoperasi.app.feature.printer.viewmodel

import androidx.lifecycle.ViewModel
import com.kasirkoperasi.app.feature.printer.state.PrinterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrinterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PrinterUiState())
    val uiState: StateFlow<PrinterUiState> = _uiState.asStateFlow()
}
