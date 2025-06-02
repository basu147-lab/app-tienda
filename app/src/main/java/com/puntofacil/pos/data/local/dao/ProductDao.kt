package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de productos en la base de datos
 */
@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE barcode = :barcode AND is_active = 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    
    @Query("SELECT * FROM products WHERE category_id = :categoryId AND is_active = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>
    
    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') 
        AND is_active = 1 
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE stock <= min_stock AND track_stock = 1 AND is_active = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE stock <= 0 AND track_stock = 1 AND is_active = 1")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    fun getActiveProductCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM products WHERE stock <= min_stock AND track_stock = 1 AND is_active = 1")
    fun getLowStockCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM products WHERE stock <= 0 AND track_stock = 1 AND is_active = 1")
    fun getOutOfStockCount(): Flow<Int>
    
    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: String, quantity: Int)
    
    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: String, quantity: Int)
    
    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int)
    
    @Query("UPDATE products SET is_active = 0 WHERE id = :productId")
    suspend fun deactivateProduct(productId: String)
    
    @Query("UPDATE products SET is_active = 1 WHERE id = :productId")
    suspend fun activateProduct(productId: String)
    
    @Query("UPDATE products SET sync_status = :status WHERE id = :productId")
    suspend fun updateSyncStatus(productId: String, status: String)
    
    @Query("SELECT * FROM products WHERE sync_status = 'pending'")
    suspend fun getPendingSyncProducts(): List<ProductEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    @Transaction
    suspend fun upsertProduct(product: ProductEntity) {
        val existingProduct = getProductById(product.id)
        if (existingProduct != null) {
            updateProduct(product.copy(createdAt = existingProduct.createdAt))
        } else {
            insertProduct(product)
        }
    }
    
    @Transaction
    suspend fun sellProduct(productId: String, quantity: Int): Boolean {
        val product = getProductById(productId)
        return if (product != null && (product.allowNegativeStock || product.stock >= quantity)) {
            decreaseStock(productId, quantity)
            true
        } else {
            false
        }
    }
}