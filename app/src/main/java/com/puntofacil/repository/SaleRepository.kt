package com.puntofacil.repository

import com.puntofacil.database.dao.SaleDao
import com.puntofacil.database.dao.SaleItemDao
import com.puntofacil.database.dao.ProductDao
import com.puntofacil.database.entity.SaleEntity
import com.puntofacil.database.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val productDao: ProductDao
) {
    
    // ========== SALE RETRIEVAL ==========
    
    fun getAllSales(): Flow<List<SaleEntity>> = saleDao.getAllSales()
    
    suspend fun getSaleById(id: String): SaleEntity? = saleDao.getSaleById(id)
    
    suspend fun getSaleByReceiptNumber(receiptNumber: String): SaleEntity? = 
        saleDao.getSaleByReceiptNumber(receiptNumber)
    
    fun getSalesByDateRange(startDate: Date, endDate: Date): Flow<List<SaleEntity>> = 
        saleDao.getSalesByDateRange(startDate, endDate)
    
    fun getSalesByUser(userId: String): Flow<List<SaleEntity>> = 
        saleDao.getSalesByUser(userId)
    
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>> = 
        saleDao.getSalesByCustomer(customerId)
    
    fun getSalesByStatus(status: String): Flow<List<SaleEntity>> = 
        saleDao.getSalesByStatus(status)
    
    fun getRecentSales(limit: Int = 50): Flow<List<SaleEntity>> = 
        saleDao.getRecentSales(limit)
    
    // ========== SALE STATISTICS ==========
    
    suspend fun getTodaysSalesCount(): Int = saleDao.getTodaysSalesCount()
    
    suspend fun getTodaysSalesTotal(): BigDecimal = saleDao.getTodaysSalesTotal() ?: BigDecimal.ZERO
    
    suspend fun getSalesCountByDateRange(startDate: Date, endDate: Date): Int = 
        saleDao.getSalesCountByDateRange(startDate, endDate)
    
    suspend fun getSalesTotalByDateRange(startDate: Date, endDate: Date): BigDecimal = 
        saleDao.getSalesTotalByDateRange(startDate, endDate) ?: BigDecimal.ZERO
    
    suspend fun getAverageSale(): BigDecimal = saleDao.getAverageSale() ?: BigDecimal.ZERO
    
    fun getPaymentMethodStats(startDate: Date, endDate: Date): Flow<List<PaymentMethodStat>> = 
        saleDao.getPaymentMethodStats(startDate, endDate).map { stats ->
            stats.map { stat ->
                PaymentMethodStat(
                    paymentMethod = stat.paymentMethod,
                    count = stat.count,
                    total = stat.total ?: BigDecimal.ZERO
                )
            }
        }
    
    // ========== SALE CREATION ==========
    
    suspend fun createSale(
        customerId: String? = null,
        userId: String,
        items: List<SaleItemRequest>,
        paymentMethod: String,
        cashReceived: BigDecimal? = null,
        cardLastFourDigits: String? = null,
        cardType: String? = null,
        transactionId: String? = null,
        discountAmount: BigDecimal = BigDecimal.ZERO,
        notes: String? = null
    ): SaleResult {
        return try {
            // Validate items and calculate totals
            val validationResult = validateSaleItems(items)
            if (!validationResult.isValid) {
                return SaleResult.Error(validationResult.errors.joinToString(", "))
            }
            
            val subtotal = items.sumOf { item ->
                item.price.multiply(BigDecimal(item.quantity))
            }
            
            val taxAmount = items.sumOf { item ->
                val lineTotal = item.price.multiply(BigDecimal(item.quantity))
                lineTotal.multiply(item.taxRate).divide(BigDecimal(100))
            }
            
            val total = subtotal.plus(taxAmount).minus(discountAmount)
            
            val changeAmount = if (paymentMethod == "cash" && cashReceived != null) {
                cashReceived.minus(total).takeIf { it >= BigDecimal.ZERO } ?: BigDecimal.ZERO
            } else {
                BigDecimal.ZERO
            }
            
            // Generate receipt number
            val receiptNumber = generateReceiptNumber()
            
            // Create sale entity
            val saleId = UUID.randomUUID().toString()
            val sale = SaleEntity(
                id = saleId,
                receiptNumber = receiptNumber,
                customerId = customerId,
                userId = userId,
                saleDate = Date(),
                subtotal = subtotal,
                taxAmount = taxAmount,
                discountAmount = discountAmount,
                total = total,
                paymentMethod = paymentMethod,
                paymentStatus = "completed",
                cashReceived = cashReceived,
                changeAmount = changeAmount,
                cardLastFourDigits = cardLastFourDigits,
                cardType = cardType,
                transactionId = transactionId,
                status = "completed",
                notes = notes,
                isRefunded = false,
                refundAmount = BigDecimal.ZERO,
                refundReason = null,
                refundDate = null,
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "pending",
                lastSyncAt = null
            )
            
            // Insert sale
            saleDao.insertSale(sale)
            
            // Create and insert sale items
            val saleItems = items.map { item ->
                val lineTotal = item.price.multiply(BigDecimal(item.quantity))
                val lineTax = lineTotal.multiply(item.taxRate).divide(BigDecimal(100))
                
                SaleItemEntity(
                    id = UUID.randomUUID().toString(),
                    saleId = saleId,
                    productId = item.productId,
                    productName = item.productName,
                    productBarcode = item.productBarcode,
                    quantity = item.quantity,
                    unitPrice = item.price,
                    unitCost = item.cost,
                    discountAmount = BigDecimal.ZERO,
                    taxAmount = lineTax,
                    lineTotal = lineTotal.plus(lineTax),
                    notes = null,
                    isRefunded = false,
                    refundQuantity = 0,
                    refundAmount = BigDecimal.ZERO,
                    refundDate = null,
                    createdAt = Date(),
                    updatedAt = Date(),
                    syncStatus = "pending",
                    lastSyncAt = null
                )
            }
            
            saleItemDao.insertSaleItems(saleItems)
            
            // Update product stock
            items.forEach { item ->
                productDao.sellProduct(item.productId, item.quantity)
            }
            
            SaleResult.Success(sale, saleItems)
            
        } catch (e: Exception) {
            SaleResult.Error("Failed to create sale: ${e.message}")
        }
    }
    
    // ========== SALE MANAGEMENT ==========
    
    suspend fun cancelSale(saleId: String, reason: String): Boolean {
        return try {
            val sale = getSaleById(saleId) ?: return false
            if (sale.status != "completed") return false
            
            // Restore product stock
            val saleItems = saleItemDao.getItemsBySaleId(saleId)
            saleItems.forEach { item ->
                productDao.increaseStock(item.productId, item.quantity)
            }
            
            // Update sale status
            saleDao.cancelSale(saleId, reason)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun refundSale(
        saleId: String, 
        refundAmount: BigDecimal, 
        reason: String
    ): Boolean {
        return try {
            val sale = getSaleById(saleId) ?: return false
            if (sale.status != "completed" || sale.isRefunded) return false
            
            // Restore product stock
            val saleItems = saleItemDao.getItemsBySaleId(saleId)
            saleItems.forEach { item ->
                productDao.increaseStock(item.productId, item.quantity)
                saleItemDao.refundSaleItem(item.id, item.quantity, item.lineTotal)
            }
            
            // Update sale with refund info
            saleDao.refundSale(saleId, refundAmount, reason)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun partialRefund(
        saleId: String,
        itemRefunds: List<ItemRefund>
    ): Boolean {
        return try {
            val sale = getSaleById(saleId) ?: return false
            if (sale.status != "completed") return false
            
            var totalRefundAmount = BigDecimal.ZERO
            
            itemRefunds.forEach { refund ->
                val saleItem = saleItemDao.getItemById(refund.saleItemId) ?: return false
                
                // Validate refund quantity
                val availableQuantity = saleItem.quantity - saleItem.refundQuantity
                if (refund.quantity > availableQuantity) return false
                
                // Calculate refund amount for this item
                val unitRefund = saleItem.lineTotal.divide(BigDecimal(saleItem.quantity))
                val itemRefundAmount = unitRefund.multiply(BigDecimal(refund.quantity))
                totalRefundAmount = totalRefundAmount.plus(itemRefundAmount)
                
                // Restore stock
                productDao.increaseStock(saleItem.productId, refund.quantity)
                
                // Update sale item
                saleItemDao.refundSaleItem(refund.saleItemId, refund.quantity, itemRefundAmount)
            }
            
            // Update sale refund amount
            val currentRefund = sale.refundAmount
            val newRefundAmount = currentRefund.plus(totalRefundAmount)
            saleDao.updateRefundAmount(saleId, newRefundAmount)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== RECEIPT OPERATIONS ==========
    
    private suspend fun generateReceiptNumber(): String {
        val today = Date()
        val datePrefix = android.text.format.DateFormat.format("yyyyMMdd", today).toString()
        val todaysCount = getTodaysSalesCount() + 1
        return "$datePrefix-${todaysCount.toString().padStart(4, '0')}"
    }
    
    suspend fun getSaleWithItems(saleId: String): SaleWithItems? {
        val sale = getSaleById(saleId) ?: return null
        val items = saleItemDao.getItemsBySaleId(saleId)
        return SaleWithItems(sale, items)
    }
    
    // ========== SYNC OPERATIONS ==========
    
    suspend fun getPendingSyncSales(): List<SaleEntity> = saleDao.getPendingSyncSales()
    
    suspend fun markAsSynced(saleId: String): Boolean {
        return try {
            saleDao.updateSyncStatus(saleId, "synced")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== VALIDATION ==========
    
    private suspend fun validateSaleItems(items: List<SaleItemRequest>): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (items.isEmpty()) {
            errors.add("Sale must have at least one item")
            return ValidationResult(false, errors)
        }
        
        items.forEachIndexed { index, item ->
            if (item.quantity <= 0) {
                errors.add("Item ${index + 1}: Quantity must be greater than zero")
            }
            
            if (item.price <= BigDecimal.ZERO) {
                errors.add("Item ${index + 1}: Price must be greater than zero")
            }
            
            // Check stock availability
            val product = productDao.getProductById(item.productId)
            if (product == null) {
                errors.add("Item ${index + 1}: Product not found")
            } else if (product.trackStock && product.currentStock < item.quantity) {
                errors.add("Item ${index + 1}: Insufficient stock (available: ${product.currentStock})")
            }
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    // ========== DATA CLASSES ==========
    
    data class SaleItemRequest(
        val productId: String,
        val productName: String,
        val productBarcode: String?,
        val quantity: Int,
        val price: BigDecimal,
        val cost: BigDecimal,
        val taxRate: BigDecimal = BigDecimal.ZERO
    )
    
    data class ItemRefund(
        val saleItemId: String,
        val quantity: Int
    )
    
    data class PaymentMethodStat(
        val paymentMethod: String,
        val count: Int,
        val total: BigDecimal
    )
    
    data class SaleWithItems(
        val sale: SaleEntity,
        val items: List<SaleItemEntity>
    )
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
    
    sealed class SaleResult {
        data class Success(
            val sale: SaleEntity,
            val items: List<SaleItemEntity>
        ) : SaleResult()
        
        data class Error(val message: String) : SaleResult()
    }
}