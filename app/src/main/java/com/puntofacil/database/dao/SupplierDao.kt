package com.puntofacil.database.dao

import androidx.room.*
import com.puntofacil.database.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SupplierDao {
    
    // ========== SELECT QUERIES ==========
    
    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveSuppliers(): Flow<List<SupplierEntity>>
    
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>
    
    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: String): SupplierEntity?
    
    @Query("SELECT * FROM suppliers WHERE email = :email LIMIT 1")
    suspend fun getSupplierByEmail(email: String): SupplierEntity?
    
    @Query("SELECT * FROM suppliers WHERE phone = :phone LIMIT 1")
    suspend fun getSupplierByPhone(phone: String): SupplierEntity?
    
    @Query("SELECT * FROM suppliers WHERE taxId = :taxId LIMIT 1")
    suspend fun getSupplierByTaxId(taxId: String): SupplierEntity?
    
    @Query("""
        SELECT * FROM suppliers 
        WHERE isActive = 1 AND (
            name LIKE '%' || :query || '%' OR 
            contactPerson LIKE '%' || :query || '%' OR
            email LIKE '%' || :query || '%' OR
            phone LIKE '%' || :query || '%'
        )
        ORDER BY name ASC
    """)
    fun searchActiveSuppliers(query: String): Flow<List<SupplierEntity>>
    
    @Query("""
        SELECT * FROM suppliers 
        WHERE name LIKE '%' || :query || '%' OR 
              contactPerson LIKE '%' || :query || '%' OR
              email LIKE '%' || :query || '%' OR
              phone LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchAllSuppliers(query: String): Flow<List<SupplierEntity>>
    
    @Query("SELECT * FROM suppliers WHERE isActive = 0 ORDER BY name ASC")
    fun getInactiveSuppliers(): Flow<List<SupplierEntity>>
    
    @Query("SELECT * FROM suppliers WHERE creditLimit > 0 AND isActive = 1 ORDER BY creditLimit DESC")
    fun getSuppliersWithCredit(): Flow<List<SupplierEntity>>
    
    // ========== COUNT QUERIES ==========
    
    @Query("SELECT COUNT(*) FROM suppliers WHERE isActive = 1")
    suspend fun getActiveSupplierCount(): Int
    
    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getTotalSupplierCount(): Int
    
    @Query("SELECT COUNT(*) FROM suppliers WHERE isActive = 0")
    suspend fun getInactiveSupplierCount(): Int
    
    // ========== UPDATE QUERIES ==========
    
    @Query("UPDATE suppliers SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivateSupplier(id: String, updatedAt: Date = Date())
    
    @Query("UPDATE suppliers SET isActive = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun activateSupplier(id: String, updatedAt: Date = Date())
    
    @Query("UPDATE suppliers SET creditLimit = :creditLimit, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCreditLimit(id: String, creditLimit: Double, updatedAt: Date = Date())
    
    @Query("UPDATE suppliers SET paymentTerms = :paymentTerms, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePaymentTerms(id: String, paymentTerms: String, updatedAt: Date = Date())
    
    @Query("UPDATE suppliers SET notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String?, updatedAt: Date = Date())
    
    // ========== SYNC QUERIES ==========
    
    @Query("UPDATE suppliers SET syncStatus = :syncStatus, lastSyncAt = :lastSyncAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, lastSyncAt: Date = Date())
    
    @Query("SELECT * FROM suppliers WHERE syncStatus = 'pending' OR syncStatus = 'failed'")
    suspend fun getPendingSyncSuppliers(): List<SupplierEntity>
    
    @Query("UPDATE suppliers SET syncStatus = 'pending', updatedAt = :updatedAt WHERE id = :id")
    suspend fun markForSync(id: String, updatedAt: Date = Date())
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSupplier(supplier: SupplierEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>): List<Long>
    
    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)
    
    @Update
    suspend fun updateSuppliers(suppliers: List<SupplierEntity>)
    
    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)
    
    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: String)
    
    @Query("DELETE FROM suppliers")
    suspend fun deleteAllSuppliers()
    
    // ========== UPSERT OPERATIONS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupplier(supplier: SupplierEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSuppliers(suppliers: List<SupplierEntity>): List<Long>
    
    // ========== TRANSACTION OPERATIONS ==========
    
    @Transaction
    suspend fun insertOrUpdateSupplier(supplier: SupplierEntity): Long {
        val existing = getSupplierById(supplier.id)
        return if (existing != null) {
            updateSupplier(supplier.copy(createdAt = existing.createdAt))
            0L // Return 0 for update
        } else {
            insertSupplier(supplier)
        }
    }
}