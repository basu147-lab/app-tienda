package com.puntofacil.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.data.entities.Product
import com.puntofacil.data.repositories.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ProductFilter = ProductFilter.ALL,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val errorMessage: String? = null
)

enum class ProductFilter {
    ALL, LOW_STOCK, OUT_OF_STOCK
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val products = productRepository.getAllActiveProducts()
                val lowStockProducts = productRepository.getLowStockProducts()
                val outOfStockProducts = productRepository.getOutOfStockProducts()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    products = products,
                    filteredProducts = filterProducts(products, _uiState.value.selectedFilter, _uiState.value.searchQuery),
                    totalProducts = products.size,
                    lowStockCount = lowStockProducts.size,
                    outOfStockCount = outOfStockProducts.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading products: ${e.message}"
                )
            }
        }
    }

    fun searchProducts(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = filterProducts(_uiState.value.products, _uiState.value.selectedFilter, query)
        )
    }

    fun setFilter(filter: ProductFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredProducts = filterProducts(_uiState.value.products, filter, _uiState.value.searchQuery)
        )
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                productRepository.createProduct(product)
                loadProducts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error adding product: ${e.message}"
                )
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                productRepository.updateProduct(product)
                loadProducts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error updating product: ${e.message}"
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                productRepository.deleteProduct(productId)
                loadProducts() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error deleting product: ${e.message}"
                )
            }
        }
    }

    fun adjustStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val product = productRepository.getProductById(productId)
                if (product != null) {
                    val updatedProduct = product.copy(stock = newStock)
                    productRepository.updateProduct(updatedProduct)
                    loadProducts() // Refresh the list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error adjusting stock: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun filterProducts(products: List<Product>, filter: ProductFilter, searchQuery: String): List<Product> {
        var filtered = when (filter) {
            ProductFilter.ALL -> products
            ProductFilter.LOW_STOCK -> products.filter { it.stock <= it.minStock && it.stock > 0 }
            ProductFilter.OUT_OF_STOCK -> products.filter { it.stock <= 0 }
        }

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.barcode.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    fun refreshData() {
        loadProducts()
    }
}