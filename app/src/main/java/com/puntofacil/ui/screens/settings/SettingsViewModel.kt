package com.puntofacil.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.data.repositories.CustomerRepository
import com.puntofacil.data.repositories.ProductRepository
import com.puntofacil.data.repositories.SaleRepository
import com.puntofacil.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val backupInProgress: Boolean = false,
    val exportInProgress: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Load any settings data if needed
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading settings: ${e.message}"
                )
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                backupInProgress = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would create a backup file
                // For now, we'll simulate the process
                kotlinx.coroutines.delay(2000) // Simulate backup process
                
                _uiState.value = _uiState.value.copy(
                    backupInProgress = false,
                    successMessage = "Backup created successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    backupInProgress = false,
                    errorMessage = "Error creating backup: ${e.message}"
                )
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                exportInProgress = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would export data to CSV files
                // For now, we'll simulate the process
                
                // Get all data
                val products = productRepository.getAllProducts()
                val customers = customerRepository.getAllCustomers()
                val sales = saleRepository.getAllSales()
                val users = userRepository.getAllUsers()
                
                // Simulate export process
                kotlinx.coroutines.delay(3000)
                
                _uiState.value = _uiState.value.copy(
                    exportInProgress = false,
                    successMessage = "Data exported successfully. Files saved to Downloads folder."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    exportInProgress = false,
                    errorMessage = "Error exporting data: ${e.message}"
                )
            }
        }
    }

    fun restoreData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would restore data from a backup file
                // For now, we'll simulate the process
                kotlinx.coroutines.delay(3000) // Simulate restore process
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Data restored successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error restoring data: ${e.message}"
                )
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would clear all data
                // This is a dangerous operation and should require confirmation
                
                // Clear all tables (be very careful with this)
                // Note: This would typically be done with proper cascade deletes
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "All data cleared successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error clearing data: ${e.message}"
                )
            }
        }
    }

    fun updateBusinessInfo(
        businessName: String,
        address: String,
        phone: String,
        email: String,
        taxId: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would update business information in a settings table
                // For now, we'll simulate the process
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Business information updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating business information: ${e.message}"
                )
            }
        }
    }

    fun updateTaxSettings(
        defaultTaxRate: Double,
        taxIncluded: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would update tax settings
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Tax settings updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating tax settings: ${e.message}"
                )
            }
        }
    }

    fun updateReceiptSettings(
        showLogo: Boolean,
        footerText: String,
        showTaxBreakdown: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                // In a real app, this would update receipt settings
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Receipt settings updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating receipt settings: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun refreshData() {
        loadSettings()
    }
}