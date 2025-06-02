package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.SupplierEntity
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
    
    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>
    
    @Query("SELECT COUNT(*) FROM suppliers WHERE isActive = 1")
    suspend fun getActiveSuppliersCount(): Int
    
    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getTotalSuppliersCount(): Int
    
    // ========== INSERT QUERIES ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)
    
    // ========== UPDATE QUERIES ==========
    
    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)
    
    @Query("UPDATE suppliers SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivateSupplier(id: String, updatedAt: Date = Date())
    
    @Query("UPDATE suppliers SET isActive = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun activateSupplier(id: String, updatedAt: Date = Date())
    
    // ========== DELETE QUERIES ==========
    
    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)
    
    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: String)
    
    @Query("DELETE FROM suppliers WHERE isActive = 0")
    suspend fun deleteInactiveSuppliers()
    
    @Query("DELETE FROM suppliers")
    suspend fun deleteAllSuppliers()
}