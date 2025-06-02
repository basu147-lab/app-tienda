package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date

/**
 * DAO para operaciones de elementos de venta en la base de datos
 */
@Dao
interface SaleItemDao {
    
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId ORDER BY created_at ASC")
    fun getSaleItemsBySaleId(saleId: String): Flow<List<SaleItemEntity>>
    
    @Query("SELECT * FROM sale_items WHERE id = :id")
    suspend fun getSaleItemById(id: String): SaleItemEntity?
    
    @Query("SELECT * FROM sale_items WHERE product_id = :productId ORDER BY created_at DESC")
    fun getSaleItemsByProductId(productId: String): Flow<List<SaleItemEntity>>
    
    @Query("""
        SELECT si.*, p.name as current_product_name 
        FROM sale_items si 
        LEFT JOIN products p ON si.product_id = p.id 
        WHERE si.sale_id = :saleId 
        ORDER BY si.created_at ASC
    """)
    suspend fun getSaleItemsWithCurrentProductInfo(saleId: String): List<SaleItemWithProductInfo>
    
    @Query("""
        SELECT product_id, product_name, SUM(quantity) as total_quantity, 
               SUM(line_total) as total_sales, COUNT(*) as sale_count
        FROM sale_items si
        INNER JOIN sales s ON si.sale_id = s.id
        WHERE s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'completed'
        AND si.is_refunded = 0
        GROUP BY product_id, product_name
        ORDER BY total_quantity DESC
    """)
    suspend fun getTopSellingProducts(startDate: Date, endDate: Date): List<ProductSalesStats>
    
    @Query("""
        SELECT product_id, product_name, SUM(line_total) as total_revenue
        FROM sale_items si
        INNER JOIN sales s ON si.sale_id = s.id
        WHERE s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'completed'
        AND si.is_refunded = 0
        GROUP BY product_id, product_name
        ORDER BY total_revenue DESC
    """)
    suspend fun getTopRevenueProducts(startDate: Date, endDate: Date): List<ProductRevenueStats>
    
    @Query("""
        SELECT COALESCE(SUM(quantity), 0) FROM sale_items si
        INNER JOIN sales s ON si.sale_id = s.id
        WHERE si.product_id = :productId
        AND s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'completed'
        AND si.is_refunded = 0
    """)
    suspend fun getTotalQuantitySoldByProduct(productId: String, startDate: Date, endDate: Date): Int
    
    @Query("""
        SELECT COALESCE(SUM(line_total), 0) FROM sale_items si
        INNER JOIN sales s ON si.sale_id = s.id
        WHERE si.product_id = :productId
        AND s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'completed'
        AND si.is_refunded = 0
    """)
    suspend fun getTotalRevenueByProduct(productId: String, startDate: Date, endDate: Date): BigDecimal
    
    @Query("""
        SELECT COALESCE(AVG(unit_price), 0) FROM sale_items si
        INNER JOIN sales s ON si.sale_id = s.id
        WHERE si.product_id = :productId
        AND s.sale_date BETWEEN :startDate AND :endDate
        AND s.status = 'completed'
        AND si.is_refunded = 0
    """)
    suspend fun getAveragePriceByProduct(productId: String, startDate: Date, endDate: Date): BigDecimal
    
    @Query("""
        UPDATE sale_items 
        SET is_refunded = 1, refunded_quantity = :quantity, refund_reason = :reason 
        WHERE id = :saleItemId
    """)
    suspend fun refundSaleItem(saleItemId: String, quantity: Int, reason: String)
    
    @Query("""
        UPDATE sale_items 
        SET is_refunded = 1, refunded_quantity = quantity, refund_reason = :reason 
        WHERE sale_id = :saleId
    """)
    suspend fun refundAllSaleItems(saleId: String, reason: String)
    
    @Query("UPDATE sale_items SET sync_status = :status WHERE id = :saleItemId")
    suspend fun updateSyncStatus(saleItemId: String, status: String)
    
    @Query("SELECT * FROM sale_items WHERE sync_status = 'pending'")
    suspend fun getPendingSyncSaleItems(): List<SaleItemEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(saleItem: SaleItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(saleItems: List<SaleItemEntity>)
    
    @Update
    suspend fun updateSaleItem(saleItem: SaleItemEntity)
    
    @Delete
    suspend fun deleteSaleItem(saleItem: SaleItemEntity)
    
    @Query("DELETE FROM sale_items WHERE id = :saleItemId")
    suspend fun deleteSaleItemById(saleItemId: String)
    
    @Query("DELETE FROM sale_items WHERE sale_id = :saleId")
    suspend fun deleteSaleItemsBySaleId(saleId: String)
    
    @Query("DELETE FROM sale_items")
    suspend fun deleteAllSaleItems()
    
    @Transaction
    suspend fun upsertSaleItem(saleItem: SaleItemEntity) {
        val existingSaleItem = getSaleItemById(saleItem.id)
        if (existingSaleItem != null) {
            updateSaleItem(saleItem.copy(createdAt = existingSaleItem.createdAt))
        } else {
            insertSaleItem(saleItem)
        }
    }
}

/**
 * Data class para información de elementos de venta con datos actuales del producto
 */
data class SaleItemWithProductInfo(
    val id: String,
    val sale_id: String,
    val product_id: String,
    val product_name: String,
    val current_product_name: String?,
    val quantity: Int,
    val unit_price: BigDecimal,
    val line_total: BigDecimal,
    val is_refunded: Boolean
)

/**
 * Data class para estadísticas de ventas por producto
 */
data class ProductSalesStats(
    val product_id: String,
    val product_name: String,
    val total_quantity: Int,
    val total_sales: BigDecimal,
    val sale_count: Int
)

/**
 * Data class para estadísticas de ingresos por producto
 */
data class ProductRevenueStats(
    val product_id: String,
    val product_name: String,
    val total_revenue: BigDecimal
)