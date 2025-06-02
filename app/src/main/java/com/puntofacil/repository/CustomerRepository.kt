package com.puntofacil.repository

import com.puntofacil.pos.data.local.dao.CustomerDao
import com.puntofacil.pos.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    
    // ========== CUSTOMER RETRIEVAL ==========
    
    fun getAllActiveCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllActiveCustomers()
    
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    
    suspend fun getCustomerById(id: String): CustomerEntity? = customerDao.getCustomerById(id)
    
    suspend fun getCustomerByEmail(email: String): CustomerEntity? = customerDao.getCustomerByEmail(email)
    
    suspend fun getCustomerByPhone(phone: String): CustomerEntity? = customerDao.getCustomerByPhone(phone)
    
    suspend fun getCustomerByDocumentNumber(documentNumber: String): CustomerEntity? = 
        customerDao.getCustomerByDocumentNumber(documentNumber)
    
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = 
        customerDao.searchCustomers(query)
    
    fun getCustomersByGroup(group: String): Flow<List<CustomerEntity>> = 
        customerDao.getCustomersByGroup(group)
    
    fun getHighValueCustomers(minSpent: BigDecimal = BigDecimal(1000)): Flow<List<CustomerEntity>> = 
        customerDao.getHighValueCustomers(minSpent)
    
    fun getCustomersWithLoyaltyPoints(): Flow<List<CustomerEntity>> = 
        customerDao.getCustomersWithLoyaltyPoints()
    
    fun getInactiveCustomers(): Flow<List<CustomerEntity>> = 
        customerDao.getInactiveCustomers()
    
    fun getCustomersWithBirthdayToday(): Flow<List<CustomerEntity>> = 
        customerDao.getCustomersWithBirthdayToday()
    
    // ========== CUSTOMER MANAGEMENT ==========
    
    suspend fun createCustomer(
        firstName: String,
        lastName: String,
        email: String? = null,
        phone: String? = null,
        address: String? = null,
        city: String? = null,
        state: String? = null,
        zipCode: String? = null,
        country: String? = null,
        documentType: String? = null,
        documentNumber: String? = null,
        birthDate: Date? = null,
        gender: String? = null,
        notes: String? = null,
        customerGroup: String? = null,
        preferredContactMethod: String? = null,
        marketingConsent: Boolean = false
    ): CreateCustomerResult {
        return try {
            // Validate customer data
            val validationResult = validateCustomerData(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                documentNumber = documentNumber
            )
            
            if (!validationResult.isValid) {
                return CreateCustomerResult.Error(validationResult.errors.joinToString(", "))
            }
            
            val customer = CustomerEntity(
                id = UUID.randomUUID().toString(),
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                address = address,
                city = city,
                state = state,
                zipCode = zipCode,
                country = country,
                documentType = documentType,
                documentNumber = documentNumber,
                birthDate = birthDate,
                gender = gender,
                notes = notes,
                loyaltyPoints = 0,
                totalSpent = BigDecimal.ZERO,
                totalVisits = 0,
                lastVisit = null,
                isActive = true,
                marketingConsent = marketingConsent,
                preferredContactMethod = preferredContactMethod,
                customerGroup = customerGroup,
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "pending",
                lastSyncAt = null
            )
            
            val id = customerDao.insertCustomer(customer)
            CreateCustomerResult.Success(customer, id)
            
        } catch (e: Exception) {
            CreateCustomerResult.Error("Failed to create customer: ${e.message}")
        }
    }
    
    suspend fun updateCustomer(customer: CustomerEntity): Boolean {
        return try {
            val validationResult = validateCustomerData(
                firstName = customer.firstName,
                lastName = customer.lastName,
                email = customer.email,
                phone = customer.phone,
                documentNumber = customer.documentNumber,
                excludeCustomerId = customer.id
            )
            
            if (!validationResult.isValid) {
                return false
            }
            
            val updatedCustomer = customer.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
            customerDao.updateCustomer(updatedCustomer)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteCustomer(customerId: String): Boolean {
        return try {
            customerDao.deleteCustomerById(customerId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun activateCustomer(customerId: String): Boolean {
        return try {
            customerDao.activateCustomer(customerId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deactivateCustomer(customerId: String): Boolean {
        return try {
            customerDao.deactivateCustomer(customerId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== CUSTOMER STATISTICS ==========
    
    suspend fun updateCustomerStats(
        customerId: String,
        saleAmount: BigDecimal
    ): Boolean {
        return try {
            customerDao.updateTotalSpent(customerId, saleAmount)
            customerDao.updateTotalVisits(customerId)
            customerDao.updateLastVisit(customerId, Date())
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== LOYALTY POINTS ==========
    
    suspend fun addLoyaltyPoints(
        customerId: String,
        points: Int,
        reason: String = "Purchase"
    ): Boolean {
        return try {
            customerDao.addLoyaltyPoints(customerId, points)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun redeemLoyaltyPoints(
        customerId: String,
        points: Int,
        reason: String = "Redemption"
    ): Boolean {
        return try {
            val customer = getCustomerById(customerId) ?: return false
            if (customer.loyaltyPoints >= points) {
                customerDao.redeemLoyaltyPoints(customerId, points)
                true
            } else {
                false // Insufficient points
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun calculateLoyaltyPoints(saleAmount: BigDecimal): Int {
        // 1 point per dollar spent (configurable)
        return saleAmount.toInt()
    }
    
    // ========== VALIDATION ==========
    
    suspend fun validateCustomerData(
        firstName: String,
        lastName: String,
        email: String? = null,
        phone: String? = null,
        documentNumber: String? = null,
        excludeCustomerId: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (firstName.isBlank()) {
            errors.add("First name is required")
        }
        
        if (lastName.isBlank()) {
            errors.add("Last name is required")
        }
        
        if (email != null && email.isNotBlank()) {
            if (!isValidEmail(email)) {
                errors.add("Invalid email format")
            } else {
                val existingCustomer = getCustomerByEmail(email)
                if (existingCustomer != null && existingCustomer.id != excludeCustomerId) {
                    errors.add("Email already exists")
                }
            }
        }
        
        if (phone != null && phone.isNotBlank()) {
            if (!isValidPhone(phone)) {
                errors.add("Invalid phone format")
            } else {
                val existingCustomer = getCustomerByPhone(phone)
                if (existingCustomer != null && existingCustomer.id != excludeCustomerId) {
                    errors.add("Phone number already exists")
                }
            }
        }
        
        if (documentNumber != null && documentNumber.isNotBlank()) {
            val existingCustomer = getCustomerByDocumentNumber(documentNumber)
            if (existingCustomer != null && existingCustomer.id != excludeCustomerId) {
                errors.add("Document number already exists")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhone(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }
    
    // ========== SYNC OPERATIONS ==========
    
    suspend fun getPendingSyncCustomers(): List<CustomerEntity> = 
        customerDao.getPendingSyncCustomers()
    
    suspend fun markAsSynced(customerId: String): Boolean {
        return try {
            customerDao.updateSyncStatus(customerId, "synced")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun markSyncFailed(customerId: String): Boolean {
        return try {
            customerDao.updateSyncStatus(customerId, "failed")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== BULK OPERATIONS ==========
    
    suspend fun bulkInsertCustomers(customers: List<CustomerEntity>): List<Long> {
        return customerDao.insertCustomers(customers)
    }
    
    suspend fun bulkUpdateCustomers(customers: List<CustomerEntity>) {
        val updatedCustomers = customers.map { customer ->
            customer.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
        }
        customerDao.updateCustomers(updatedCustomers)
    }
    
    suspend fun bulkDeactivateCustomers(customerIds: List<String>): Boolean {
        return try {
            customerIds.forEach { customerId ->
                customerDao.deactivateCustomer(customerId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== CUSTOMER GROUPS ==========
    
    fun getDefaultCustomerGroups(): List<String> = listOf(
        "Regular",
        "VIP",
        "Wholesale",
        "Corporate",
        "Student",
        "Senior",
        "Employee"
    )
    
    fun getPreferredContactMethods(): List<String> = listOf(
        "Email",
        "Phone",
        "SMS",
        "WhatsApp",
        "None"
    )
    
    // ========== DATA CLASSES ==========
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
    
    sealed class CreateCustomerResult {
        data class Success(
            val customer: CustomerEntity,
            val id: Long
        ) : CreateCustomerResult()
        
        data class Error(val message: String) : CreateCustomerResult()
    }
}