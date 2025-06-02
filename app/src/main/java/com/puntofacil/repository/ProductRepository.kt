package com.puntofacil.repository

import com.puntofacil.database.dao.ProductDao
import com.puntofacil.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    
    // ========== PRODUCT RETRIEVAL ==========
    
    fun getAllActiveProducts(): Flow<List<ProductEntity>> = productDao.getAllActiveProducts()
    
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: String): ProductEntity? = productDao.getProductById(id)
    
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = productDao.getProductByBarcode(barcode)
    
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>> = 
        productDao.getProductsByCategory(categoryId)
    
    fun searchProducts(query: String): Flow<List<ProductEntity>> = 
        productDao.searchProducts(query)
    
    fun getLowStockProducts(threshold: Int = 10): Flow<List<ProductEntity>> = 
        productDao.getLowStockProducts(threshold)
    
    fun getOutOfStockProducts(): Flow<List<ProductEntity>> = 
        productDao.getOutOfStockProducts()
    
    // ========== PRODUCT STATISTICS ==========
    
    suspend fun getActiveProductCount(): Int = productDao.getActiveProductCount()
    
    suspend fun getLowStockCount(threshold: Int = 10): Int = productDao.getLowStockCount(threshold)
    
    suspend fun getOutOfStockCount(): Int = productDao.getOutOfStockCount()
    
    fun getInventoryValue(): Flow<BigDecimal> = 
        getAllActiveProducts().map { products ->
            products.sumOf { product ->
                product.cost.multiply(BigDecimal(product.currentStock))
            }
        }
    
    fun getRetailValue(): Flow<BigDecimal> = 
        getAllActiveProducts().map { products ->
            products.sumOf { product ->
                product.price.multiply(BigDecimal(product.currentStock))
            }
        }
    
    // ========== PRODUCT MANAGEMENT ==========
    
    suspend fun createProduct(
        name: String,
        description: String? = null,
        barcode: String? = null,
        price: BigDecimal,
        cost: BigDecimal,
        currentStock: Int = 0,
        minStock: Int = 0,
        maxStock: Int? = null,
        categoryId: String? = null,
        supplierId: String? = null,
        imageUrl: String? = null,
        isVariant: Boolean = false,
        trackStock: Boolean = true,
        taxRate: BigDecimal = BigDecimal.ZERO,
        weight: Double? = null
    ): Long {
        val product = ProductEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            barcode = barcode,
            price = price,
            cost = cost,
            currentStock = currentStock,
            minStock = minStock,
            maxStock = maxStock,
            categoryId = categoryId,
            supplierId = supplierId,
            imageUrl = imageUrl,
            isActive = true,
            isVariant = isVariant,
            trackStock = trackStock,
            taxRate = taxRate,
            weight = weight,
            createdAt = Date(),
            updatedAt = Date(),
            syncStatus = "pending",
            lastSyncAt = null
        )
        return productDao.insertProduct(product)
    }
    
    suspend fun updateProduct(product: ProductEntity): Boolean {
        return try {
            val updatedProduct = product.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
            productDao.updateProduct(updatedProduct)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            productDao.deleteProductById(productId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun activateProduct(productId: String): Boolean {
        return try {
            productDao.activateProduct(productId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deactivateProduct(productId: String): Boolean {
        return try {
            productDao.deactivateProduct(productId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== STOCK MANAGEMENT ==========
    
    suspend fun increaseStock(productId: String, quantity: Int): Boolean {
        return try {
            productDao.increaseStock(productId, quantity)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun decreaseStock(productId: String, quantity: Int): Boolean {
        return try {
            val product = getProductById(productId)
            if (product != null && product.currentStock >= quantity) {
                productDao.decreaseStock(productId, quantity)
                true
            } else {
                false // Insufficient stock
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun setStock(productId: String, quantity: Int): Boolean {
        return try {
            productDao.setStock(productId, quantity)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun adjustStock(productId: String, adjustment: Int, reason: String = "Manual adjustment"): Boolean {
        return try {
            val product = getProductById(productId) ?: return false
            val newStock = product.currentStock + adjustment
            if (newStock >= 0) {
                setStock(productId, newStock)
            } else {
                false // Cannot have negative stock
            }
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== BARCODE OPERATIONS ==========
    
    suspend fun generateBarcode(): String {
        var barcode: String
        do {
            barcode = generateRandomBarcode()
        } while (getProductByBarcode(barcode) != null)
        return barcode
    }
    
    private fun generateRandomBarcode(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        val random = (1000..9999).random()
        return "$timestamp$random"
    }
    
    suspend fun validateBarcode(barcode: String, excludeProductId: String? = null): Boolean {
        val existingProduct = getProductByBarcode(barcode)
        return existingProduct == null || existingProduct.id == excludeProductId
    }
    
    // ========== SYNC OPERATIONS ==========
    
    suspend fun getPendingSyncProducts(): List<ProductEntity> = 
        productDao.getPendingSyncProducts()
    
    suspend fun markAsSynced(productId: String): Boolean {
        return try {
            productDao.updateSyncStatus(productId, "synced")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun markSyncFailed(productId: String): Boolean {
        return try {
            productDao.updateSyncStatus(productId, "failed")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== BULK OPERATIONS ==========
    
    suspend fun bulkInsertProducts(products: List<ProductEntity>): List<Long> {
        return productDao.insertProducts(products)
    }
    
    suspend fun bulkUpdateProducts(products: List<ProductEntity>) {
        val updatedProducts = products.map { product ->
            product.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
        }
        productDao.updateProducts(updatedProducts)
    }
    
    suspend fun bulkDeactivateProducts(productIds: List<String>): Boolean {
        return try {
            productIds.forEach { productId ->
                productDao.deactivateProduct(productId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== VALIDATION ==========
    
    suspend fun validateProductData(
        name: String,
        barcode: String?,
        price: BigDecimal,
        cost: BigDecimal,
        excludeProductId: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Product name is required")
        }
        
        if (price <= BigDecimal.ZERO) {
            errors.add("Price must be greater than zero")
        }
        
        if (cost < BigDecimal.ZERO) {
            errors.add("Cost cannot be negative")
        }
        
        if (barcode != null && barcode.isNotBlank()) {
            if (!validateBarcode(barcode, excludeProductId)) {
                errors.add("Barcode already exists")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}