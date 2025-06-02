package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.SaleEntity
import com.puntofacil.pos.data.local.entities.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date

/**
 * DAO para operaciones de ventas en la base de datos
 */
@Dao
interface SaleDao {
    
    @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
    
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?
    
    @Query("SELECT * FROM sales WHERE receipt_number = :receiptNumber")
    suspend fun getSaleByReceiptNumber(receiptNumber: String): SaleEntity?
    
    @Query("""
        SELECT * FROM sales 
        WHERE sale_date BETWEEN :startDate AND :endDate 
        ORDER BY sale_date DESC
    """)
    fun getSalesByDateRange(startDate: Date, endDate: Date): Flow<List<SaleEntity>>
    
    @Query("""
        SELECT * FROM sales 
        WHERE user_id = :userId 
        ORDER BY sale_date DESC
    """)
    fun getSalesByUser(userId: String): Flow<List<SaleEntity>>
    
    @Query("""
        SELECT * FROM sales 
        WHERE customer_id = :customerId 
        ORDER BY sale_date DESC
    """)
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>>
    
    @Query("""
        SELECT * FROM sales 
        WHERE status = :status 
        ORDER BY sale_date DESC
    """)
    fun getSalesByStatus(status: String): Flow<List<SaleEntity>>
    
    @Query("SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = DATE('now', 'localtime')")
    fun getTodaySalesCount(): Flow<Int>
    
    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = DATE('now', 'localtime') AND status = 'completed'")
    fun getTodayTotalSales(): Flow<BigDecimal>
    
    @Query("""
        SELECT COALESCE(SUM(total_amount), 0) FROM sales 
        WHERE sale_date BETWEEN :startDate AND :endDate 
        AND status = 'completed'
    """)
    suspend fun getTotalSalesByDateRange(startDate: Date, endDate: Date): BigDecimal
    
    @Query("""
        SELECT COUNT(*) FROM sales 
        WHERE sale_date BETWEEN :startDate AND :endDate 
        AND status = 'completed'
    """)
    suspend fun getSalesCountByDateRange(startDate: Date, endDate: Date): Int
    
    @Query("""
        SELECT COALESCE(AVG(total_amount), 0) FROM sales 
        WHERE sale_date BETWEEN :startDate AND :endDate 
        AND status = 'completed'
    """)
    suspend fun getAverageSaleByDateRange(startDate: Date, endDate: Date): BigDecimal
    
    @Query("""
        SELECT payment_method, COUNT(*) as count, SUM(total_amount) as total 
        FROM sales 
        WHERE sale_date BETWEEN :startDate AND :endDate 
        AND status = 'completed'
        GROUP BY payment_method
    """)
    suspend fun getPaymentMethodStats(startDate: Date, endDate: Date): List<PaymentMethodStat>
    
    @Query("""
        SELECT * FROM sales 
        WHERE sale_date >= :date 
        ORDER BY sale_date DESC 
        LIMIT :limit
    """)
    fun getRecentSales(date: Date, limit: Int = 10): Flow<List<SaleEntity>>
    
    @Query("UPDATE sales SET status = 'cancelled' WHERE id = :saleId")
    suspend fun cancelSale(saleId: String)
    
    @Query("UPDATE sales SET is_refunded = 1, refund_reason = :reason, refunded_at = :refundDate WHERE id = :saleId")
    suspend fun refundSale(saleId: String, reason: String, refundDate: Date)
    
    @Query("UPDATE sales SET sync_status = :status WHERE id = :saleId")
    suspend fun updateSyncStatus(saleId: String, status: String)
    
    @Query("SELECT * FROM sales WHERE sync_status = 'pending'")
    suspend fun getPendingSyncSales(): List<SaleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)
    
    @Update
    suspend fun updateSale(sale: SaleEntity)
    
    @Delete
    suspend fun deleteSale(sale: SaleEntity)
    
    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: String)
    
    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
    
    @Transaction
    suspend fun upsertSale(sale: SaleEntity) {
        val existingSale = getSaleById(sale.id)
        if (existingSale != null) {
            updateSale(sale.copy(createdAt = existingSale.createdAt))
        } else {
            insertSale(sale)
        }
    }
}

/**
 * Data class para estadísticas de métodos de pago
 */
data class PaymentMethodStat(
    val payment_method: String,
    val count: Int,
    val total: BigDecimal
)