package com.example.autopark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataImportExportViewModel @Inject constructor(
    private val dataImportExportRepository: com.example.autopark.data.repository.DataImportExportRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson.asStateFlow()

    private val _importResult = MutableStateFlow<com.example.autopark.data.repository.DataImportExportRepository.ImportResult?>(null)
    val importResult: StateFlow<com.example.autopark.data.repository.DataImportExportRepository.ImportResult?> = _importResult.asStateFlow()

    /**
     * Export all data as JSON
     */
    fun exportAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _progress.value = 0
            
            try {
                val result = dataImportExportRepository.exportAllData()
                result.onSuccess { json ->
                    _exportedJson.value = json
                    _successMessage.value = "Export completed successfully"
                    _errorMessage.value = null
                    _progress.value = 100
                }.onFailure { error ->
                    _errorMessage.value = "Export failed: ${error.message}"
                    _successMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Export failed: ${e.message}"
                _successMessage.value = null
            } finally {
                _isLoading.value = false
                _progress.value = 0
            }
        }
    }

    /**
     * Export summary data
     */
    fun exportSummaryData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val result = dataImportExportRepository.exportSummaryData()
                result.onSuccess { json ->
                    _exportedJson.value = json
                    _successMessage.value = "Summary export completed successfully"
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = "Summary export failed: ${error.message}"
                    _successMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Summary export failed: ${e.message}"
                _successMessage.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Import data from JSON
     */
    fun importData(jsonData: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _progress.value = 0
            
            try {
                val result = dataImportExportRepository.importData(jsonData)
                result.onSuccess { importResult ->
                    _importResult.value = importResult
                    _successMessage.value = "Import completed: ${importResult.successCount} items imported, ${importResult.errorCount} errors"
                    _errorMessage.value = if (importResult.errorCount > 0) {
                        "Some items failed to import. Check details."
                    } else null
                    _progress.value = 100
                }.onFailure { error ->
                    _errorMessage.value = "Import failed: ${error.message}"
                    _successMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Import failed: ${e.message}"
                _successMessage.value = null
            } finally {
                _isLoading.value = false
                _progress.value = 0
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _successMessage.value = null
        _importResult.value = null
    }

    fun clearExportedData() {
        _exportedJson.value = null
    }

    /**
     * Get exported data as formatted JSON string
     */
    fun getExportedData(): String? {
        return _exportedJson.value
    }
}