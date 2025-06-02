package com.puntofacil.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.data.repositories.CustomerRepository
import com.puntofacil.data.repositories.ProductRepository
import com.puntofacil.data.repositories.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class SaleInfo(
    val id: String,
    val total: Double,
    val itemCount: Int,
    val timestamp: Long
)

data class ReportsUiState(
    val isLoading: Boolean = false,
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val averageSale: Double = 0.0,
    val totalItemsSold: Int = 0,
    val topProduct: String? = null,
    val topProductSales: Int = 0,
    val totalProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val outOfStockProducts: Int = 0,
    val totalCustomers: Int = 0,
    val newCustomers: Int = 0,
    val recentSales: List<SaleInfo> = emptyList(),
    val errorMessage: String? = null
)

enum class ReportPeriod(val displayName: String) {
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month"),
    YEAR("This Year")
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports(ReportPeriod.TODAY)
    }

    fun loadReports(period: ReportPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val (startDate, endDate) = getDateRange(period)
                
                // Load sales data
                val sales = saleRepository.getSalesByDateRange(startDate, endDate)
                val allSales = saleRepository.getAllSales()
                
                // Calculate sales metrics
                val totalSales = sales.sumOf { it.total }
                val totalTransactions = sales.size
                val averageSale = if (totalTransactions > 0) totalSales / totalTransactions else 0.0
                
                // Calculate items sold
                val totalItemsSold = sales.sumOf { sale ->
                    saleRepository.getSaleItems(sale.id).sumOf { it.quantity }
                }
                
                // Find top product
                val productSales = mutableMapOf<String, Int>()
                sales.forEach { sale ->
                    saleRepository.getSaleItems(sale.id).forEach { item ->
                        productSales[item.productId] = productSales.getOrDefault(item.productId, 0) + item.quantity
                    }
                }
                val topProductEntry = productSales.maxByOrNull { it.value }
                val topProduct = topProductEntry?.let { entry ->
                    productRepository.getProductById(entry.key)?.name
                }
                val topProductSales = topProductEntry?.value ?: 0
                
                // Load inventory data
                val allProducts = productRepository.getAllActiveProducts()
                val lowStockProducts = productRepository.getLowStockProducts()
                val outOfStockProducts = productRepository.getOutOfStockProducts()
                
                // Load customer data
                val allCustomers = customerRepository.getAllCustomers()
                val newCustomers = customerRepository.getCustomersByDateRange(startDate, endDate)
                
                // Recent sales info
                val recentSales = allSales.takeLast(10).map { sale ->
                    val itemCount = saleRepository.getSaleItems(sale.id).sumOf { it.quantity }
                    SaleInfo(
                        id = sale.id,
                        total = sale.total,
                        itemCount = itemCount,
                        timestamp = sale.createdAt
                    )
                }.reversed()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalSales = totalSales,
                    totalTransactions = totalTransactions,
                    averageSale = averageSale,
                    totalItemsSold = totalItemsSold,
                    topProduct = topProduct,
                    topProductSales = topProductSales,
                    totalProducts = allProducts.size,
                    lowStockProducts = lowStockProducts.size,
                    outOfStockProducts = outOfStockProducts.size,
                    totalCustomers = allCustomers.size,
                    newCustomers = newCustomers.size,
                    recentSales = recentSales
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading reports: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshData() {
        // Reload with current period - we'd need to track the current period
        loadReports(ReportPeriod.TODAY)
    }

    private fun getDateRange(period: ReportPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        when (period) {
            ReportPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            ReportPeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        val startDate = calendar.timeInMillis
        return Pair(startDate, endDate)
    }
}