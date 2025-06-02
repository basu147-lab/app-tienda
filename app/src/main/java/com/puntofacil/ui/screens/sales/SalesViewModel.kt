package com.puntofacil.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.database.entity.ProductEntity
import com.puntofacil.repository.ProductRepository
import com.puntofacil.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesUiState(
    val searchResults: List<ProductEntity> = emptyList(),
    val saleItems: List<SaleItem> = emptyList(),
    val total: Double = 0.0,
    val isProcessing: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialProducts()
    }
    
    private fun loadInitialProducts() {
        viewModelScope.launch {
            try {
                val products = productRepository.getAllActiveProducts()
                _uiState.value = _uiState.value.copy(
                    searchResults = products.take(20) // Show first 20 products
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error al cargar productos"
                )
            }
        }
    }
    
    fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadInitialProducts()
            return
        }
        
        viewModelScope.launch {
            try {
                val products = productRepository.searchProducts(query)
                _uiState.value = _uiState.value.copy(
                    searchResults = products
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Error en la búsqueda"
                )
            }
        }
    }
    
    fun addProductToSale(product: ProductEntity) {
        if (product.stock <= 0) {
            _uiState.value = _uiState.value.copy(
                message = "Producto sin stock disponible"
            )
            return
        }
        
        val currentItems = _uiState.value.saleItems.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.product.id == product.id }
        
        if (existingItemIndex >= 0) {
            // Update quantity if product already exists
            val existingItem = currentItems[existingItemIndex]
            val newQuantity = existingItem.quantity + 1
            
            if (newQuantity <= product.stock) {
                currentItems[existingItemIndex] = existingItem.copy(quantity = newQuantity)
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No hay suficiente stock disponible"
                )
                return
            }
        } else {
            // Add new item
            currentItems.add(
                SaleItem(
                    product = product,
                    quantity = 1,
                    unitPrice = product.salePrice
                )
            )
        }
        
        updateSaleItems(currentItems)
    }
    
    fun updateItemQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItemFromSale(productId)
            return
        }
        
        val currentItems = _uiState.value.saleItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.id == productId }
        
        if (itemIndex >= 0) {
            val item = currentItems[itemIndex]
            
            if (newQuantity <= item.product.stock) {
                currentItems[itemIndex] = item.copy(quantity = newQuantity)
                updateSaleItems(currentItems)
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No hay suficiente stock disponible"
                )
            }
        }
    }
    
    fun removeItemFromSale(productId: Long) {
        val currentItems = _uiState.value.saleItems.toMutableList()
        currentItems.removeAll { it.product.id == productId }
        updateSaleItems(currentItems)
    }
    
    private fun updateSaleItems(items: List<SaleItem>) {
        val total = items.sumOf { it.total }
        _uiState.value = _uiState.value.copy(
            saleItems = items,
            total = total
        )
    }
    
    fun completeSale() {
        val saleItems = _uiState.value.saleItems
        if (saleItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                message = "No hay productos en la venta"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                // Convert to SaleItemRequest format
                val saleItemRequests = saleItems.map { saleItem ->
                    SaleRepository.SaleItemRequest(
                        productId = saleItem.product.id,
                        quantity = saleItem.quantity,
                        unitPrice = saleItem.unitPrice
                    )
                }
                
                // Create the sale
                val result = saleRepository.createSale(
                    items = saleItemRequests,
                    customerId = null, // No customer for now
                    userId = 1L, // Default user ID (in real app, get from session)
                    paymentMethod = "CASH",
                    notes = null
                )
                
                when (result) {
                    is SaleRepository.SaleResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            saleItems = emptyList(),
                            total = 0.0,
                            message = "Venta completada exitosamente. Recibo: ${result.receiptNumber}"
                        )
                        // Refresh product list to update stock
                        loadInitialProducts()
                    }
                    is SaleRepository.SaleResult.InsufficientStock -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            message = "Stock insuficiente para: ${result.productName}"
                        )
                    }
                    is SaleRepository.SaleResult.ValidationError -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            message = "Error de validación: ${result.message}"
                        )
                    }
                    is SaleRepository.SaleResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            message = "Error al procesar la venta: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    message = "Error inesperado al procesar la venta"
                )
            }
        }
    }
    
    fun clearSale() {
        _uiState.value = _uiState.value.copy(
            saleItems = emptyList(),
            total = 0.0
        )
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}