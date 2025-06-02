package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date

/**
 * DAO para operaciones de clientes en la base de datos
 */
@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers WHERE is_active = 1 ORDER BY first_name ASC")
    fun getAllActiveCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers ORDER BY first_name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE email = :email AND is_active = 1")
    suspend fun getCustomerByEmail(email: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE phone = :phone AND is_active = 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE document_number = :documentNumber AND is_active = 1")
    suspend fun getCustomerByDocumentNumber(documentNumber: String): CustomerEntity?
    
    @Query("""
        SELECT * FROM customers 
        WHERE (first_name LIKE '%' || :query || '%' 
               OR last_name LIKE '%' || :query || '%' 
               OR email LIKE '%' || :query || '%' 
               OR phone LIKE '%' || :query || '%'
               OR document_number LIKE '%' || :query || '%') 
        AND is_active = 1 
        ORDER BY first_name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    
    @Query("SELECT COUNT(*) FROM customers WHERE is_active = 1")
    fun getActiveCustomerCount(): Flow<Int>
    
    @Query("""
        SELECT * FROM customers 
        WHERE customer_group = :group AND is_active = 1 
        ORDER BY first_name ASC
    """)
    fun getCustomersByGroup(group: String): Flow<List<CustomerEntity>>
    
    @Query("""
        SELECT * FROM customers 
        WHERE total_spent >= :minAmount AND is_active = 1 
        ORDER BY total_spent DESC
    """)
    fun getHighValueCustomers(minAmount: BigDecimal): Flow<List<CustomerEntity>>
    
    @Query("""
        SELECT * FROM customers 
        WHERE loyalty_points >= :minPoints AND is_active = 1 
        ORDER BY loyalty_points DESC
    """)
    fun getCustomersWithLoyaltyPoints(minPoints: Int): Flow<List<CustomerEntity>>
    
    @Query("""
        SELECT * FROM customers 
        WHERE last_visit < :date AND is_active = 1 
        ORDER BY last_visit ASC
    """)
    fun getInactiveCustomers(date: Date): Flow<List<CustomerEntity>>
    
    @Query("""
        SELECT * FROM customers 
        WHERE birth_date IS NOT NULL 
        AND strftime('%m-%d', birth_date/1000, 'unixepoch') = strftime('%m-%d', 'now') 
        AND is_active = 1
    """)
    fun getCustomersWithBirthdayToday(): Flow<List<CustomerEntity>>
    
    @Query("""
        UPDATE customers 
        SET total_spent = total_spent + :amount, 
            total_visits = total_visits + 1, 
            last_visit = :visitDate 
        WHERE id = :customerId
    """)
    suspend fun updateCustomerStats(customerId: String, amount: BigDecimal, visitDate: Date)
    
    @Query("""
        UPDATE customers 
        SET loyalty_points = loyalty_points + :points 
        WHERE id = :customerId
    """)
    suspend fun addLoyaltyPoints(customerId: String, points: Int)
    
    @Query("""
        UPDATE customers 
        SET loyalty_points = loyalty_points - :points 
        WHERE id = :customerId AND loyalty_points >= :points
    """)
    suspend fun redeemLoyaltyPoints(customerId: String, points: Int)
    
    @Query("UPDATE customers SET is_active = 0 WHERE id = :customerId")
    suspend fun deactivateCustomer(customerId: String)
    
    @Query("UPDATE customers SET is_active = 1 WHERE id = :customerId")
    suspend fun activateCustomer(customerId: String)
    
    @Query("UPDATE customers SET sync_status = :status WHERE id = :customerId")
    suspend fun updateSyncStatus(customerId: String, status: String)
    
    @Query("SELECT * FROM customers WHERE sync_status = 'pending'")
    suspend fun getPendingSyncCustomers(): List<CustomerEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
    
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
    
    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)
    
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
    
    @Transaction
    suspend fun upsertCustomer(customer: CustomerEntity) {
        val existingCustomer = getCustomerById(customer.id)
        if (existingCustomer != null) {
            updateCustomer(customer.copy(createdAt = existingCustomer.createdAt))
        } else {
            insertCustomer(customer)
        }
    }
}