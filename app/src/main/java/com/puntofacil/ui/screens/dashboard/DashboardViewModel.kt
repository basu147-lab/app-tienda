package com.puntofacil.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.database.entity.UserEntity
import com.puntofacil.repository.CustomerRepository
import com.puntofacil.repository.ProductRepository
import com.puntofacil.repository.SaleRepository
import com.puntofacil.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val currentUser: UserEntity? = null,
    val todaySales: Double = 0.0,
    val totalProducts: Int = 0,
    val totalCustomers: Int = 0,
    val lowStockProducts: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load current user (in a real app, this would come from session/preferences)
                val users = userRepository.getAllActiveUsers()
                val currentUser = users.firstOrNull { it.role == "ADMIN" }
                
                // Load today's sales
                val today = LocalDate.now()
                val todayStats = saleRepository.getTodayStats()
                
                // Load product count
                val productCount = productRepository.getActiveProductCount()
                
                // Load customer count
                val customerCount = customerRepository.getActiveCustomerCount()
                
                // Load low stock products
                val lowStockCount = productRepository.getLowStockProductCount()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = currentUser,
                    todaySales = todayStats.total,
                    totalProducts = productCount,
                    totalCustomers = customerCount,
                    lowStockProducts = lowStockCount,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos del dashboard"
                )
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}