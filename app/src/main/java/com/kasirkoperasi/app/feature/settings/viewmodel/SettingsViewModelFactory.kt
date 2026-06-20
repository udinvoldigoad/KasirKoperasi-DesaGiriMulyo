package com.kasirkoperasi.app.feature.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.ImportProductsCsvUseCase

class SettingsViewModelFactory(
    private val context: Context,
    private val importProductsCsvUseCase: ImportProductsCsvUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                context = context,
                importProductsCsvUseCase = importProductsCsvUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
