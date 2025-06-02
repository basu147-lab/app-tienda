package com.puntofacil.repository

import com.puntofacil.database.dao.UserDao
import com.puntofacil.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    // ========== USER RETRIEVAL ==========
    
    fun getAllActiveUsers(): Flow<List<UserEntity>> = userDao.getAllActiveUsers()
    
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    
    suspend fun getUserById(id: String): UserEntity? = userDao.getUserById(id)
    
    suspend fun getUserByUsername(username: String): UserEntity? = userDao.getUserByUsername(username)
    
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    
    suspend fun getActiveUserByUsername(username: String): UserEntity? = 
        userDao.getActiveUserByUsername(username)
    
    suspend fun getActiveUserByEmail(email: String): UserEntity? = 
        userDao.getActiveUserByEmail(email)
    
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)
    
    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)
    
    fun getInactiveUsers(): Flow<List<UserEntity>> = userDao.getInactiveUsers()
    
    fun getLockedUsers(): Flow<List<UserEntity>> = userDao.getLockedUsers()
    
    // ========== USER STATISTICS ==========
    
    suspend fun getActiveUserCount(): Int = userDao.getActiveUserCount()
    
    suspend fun getUserCountByRole(role: String): Int = userDao.getUserCountByRole(role)
    
    // ========== AUTHENTICATION ==========
    
    suspend fun authenticateUser(username: String, password: String): AuthResult {
        return try {
            val user = getActiveUserByUsername(username) ?: getActiveUserByEmail(username)
            
            if (user == null) {
                return AuthResult.Error("User not found")
            }
            
            if (user.isLocked) {
                return AuthResult.Error("Account is locked")
            }
            
            if (!user.isActive) {
                return AuthResult.Error("Account is inactive")
            }
            
            val hashedPassword = hashPassword(password)
            if (user.passwordHash != hashedPassword) {
                // Increment failed login attempts
                incrementFailedLoginAttempts(user.id)
                return AuthResult.Error("Invalid credentials")
            }
            
            // Reset failed login attempts and update last login
            resetFailedLoginAttempts(user.id)
            updateLastLogin(user.id)
            
            AuthResult.Success(user)
            
        } catch (e: Exception) {
            AuthResult.Error("Authentication failed: ${e.message}")
        }
    }
    
    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Boolean {
        return try {
            val user = getUserById(userId) ?: return false
            
            val currentHashedPassword = hashPassword(currentPassword)
            if (user.passwordHash != currentHashedPassword) {
                return false // Current password is incorrect
            }
            
            val newHashedPassword = hashPassword(newPassword)
            userDao.updatePassword(userId, newHashedPassword)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun resetPassword(userId: String, newPassword: String): Boolean {
        return try {
            val hashedPassword = hashPassword(newPassword)
            userDao.updatePassword(userId, hashedPassword)
            userDao.resetFailedLoginAttempts(userId)
            userDao.unlockUser(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    // ========== USER MANAGEMENT ==========
    
    suspend fun createUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "cashier",
        permissions: List<String> = emptyList(),
        phone: String? = null,
        address: String? = null
    ): CreateUserResult {
        return try {
            // Validate user data
            val validationResult = validateUserData(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            
            if (!validationResult.isValid) {
                return CreateUserResult.Error(validationResult.errors.joinToString(", "))
            }
            
            val hashedPassword = hashPassword(password)
            
            val user = UserEntity(
                id = UUID.randomUUID().toString(),
                username = username,
                email = email,
                passwordHash = hashedPassword,
                firstName = firstName,
                lastName = lastName,
                role = role,
                permissions = permissions,
                phone = phone,
                address = address,
                isActive = true,
                isVerified = true,
                isLocked = false,
                failedLoginAttempts = 0,
                lastLogin = null,
                lastPasswordChange = Date(),
                twoFactorEnabled = false,
                twoFactorSecret = null,
                sessionToken = null,
                tokenExpiry = null,
                preferences = emptyList(),
                createdAt = Date(),
                updatedAt = Date(),
                syncStatus = "pending",
                lastSyncAt = null
            )
            
            val id = userDao.insertUser(user)
            CreateUserResult.Success(user, id)
            
        } catch (e: Exception) {
            CreateUserResult.Error("Failed to create user: ${e.message}")
        }
    }
    
    suspend fun updateUser(user: UserEntity): Boolean {
        return try {
            val validationResult = validateUserData(
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                excludeUserId = user.id
            )
            
            if (!validationResult.isValid) {
                return false
            }
            
            val updatedUser = user.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
            userDao.updateUser(updatedUser)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            userDao.deleteUserById(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun activateUser(userId: String): Boolean {
        return try {
            userDao.activateUser(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deactivateUser(userId: String): Boolean {
        return try {
            userDao.deactivateUser(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun lockUser(userId: String): Boolean {
        return try {
            userDao.lockUser(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun unlockUser(userId: String): Boolean {
        return try {
            userDao.unlockUser(userId)
            userDao.resetFailedLoginAttempts(userId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== LOGIN ATTEMPTS ==========
    
    private suspend fun incrementFailedLoginAttempts(userId: String) {
        val user = getUserById(userId) ?: return
        val newAttempts = user.failedLoginAttempts + 1
        userDao.updateFailedLoginAttempts(userId, newAttempts)
        
        // Lock user after 5 failed attempts
        if (newAttempts >= 5) {
            userDao.lockUser(userId)
        }
    }
    
    private suspend fun resetFailedLoginAttempts(userId: String) {
        userDao.resetFailedLoginAttempts(userId)
    }
    
    private suspend fun updateLastLogin(userId: String) {
        userDao.updateLastLogin(userId, Date())
    }
    
    // ========== ROLE AND PERMISSIONS ==========
    
    suspend fun updateUserRole(userId: String, role: String): Boolean {
        return try {
            userDao.updateRole(userId, role)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateUserPermissions(userId: String, permissions: List<String>): Boolean {
        return try {
            userDao.updatePermissions(userId, permissions)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getAvailableRoles(): List<String> = listOf(
        "admin",
        "manager",
        "cashier",
        "inventory_clerk",
        "sales_rep"
    )
    
    fun getAvailablePermissions(): List<String> = listOf(
        "sales_create",
        "sales_view",
        "sales_refund",
        "inventory_create",
        "inventory_edit",
        "inventory_delete",
        "inventory_view",
        "customers_create",
        "customers_edit",
        "customers_delete",
        "customers_view",
        "reports_view",
        "reports_export",
        "settings_view",
        "settings_edit",
        "users_create",
        "users_edit",
        "users_delete",
        "users_view"
    )
    
    fun getRolePermissions(role: String): List<String> {
        return when (role) {
            "admin" -> getAvailablePermissions() // All permissions
            "manager" -> listOf(
                "sales_create", "sales_view", "sales_refund",
                "inventory_create", "inventory_edit", "inventory_view",
                "customers_create", "customers_edit", "customers_view",
                "reports_view", "reports_export",
                "settings_view", "settings_edit"
            )
            "cashier" -> listOf(
                "sales_create", "sales_view",
                "inventory_view",
                "customers_create", "customers_edit", "customers_view"
            )
            "inventory_clerk" -> listOf(
                "inventory_create", "inventory_edit", "inventory_view",
                "reports_view"
            )
            "sales_rep" -> listOf(
                "sales_create", "sales_view",
                "customers_create", "customers_edit", "customers_view",
                "reports_view"
            )
            else -> emptyList()
        }
    }
    
    // ========== VALIDATION ==========
    
    suspend fun validateUserData(
        username: String,
        email: String,
        password: String? = null,
        firstName: String,
        lastName: String,
        excludeUserId: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (username.isBlank()) {
            errors.add("Username is required")
        } else if (username.length < 3) {
            errors.add("Username must be at least 3 characters")
        } else {
            val existingUser = getUserByUsername(username)
            if (existingUser != null && existingUser.id != excludeUserId) {
                errors.add("Username already exists")
            }
        }
        
        if (email.isBlank()) {
            errors.add("Email is required")
        } else if (!isValidEmail(email)) {
            errors.add("Invalid email format")
        } else {
            val existingUser = getUserByEmail(email)
            if (existingUser != null && existingUser.id != excludeUserId) {
                errors.add("Email already exists")
            }
        }
        
        if (password != null) {
            if (password.length < 6) {
                errors.add("Password must be at least 6 characters")
            }
        }
        
        if (firstName.isBlank()) {
            errors.add("First name is required")
        }
        
        if (lastName.isBlank()) {
            errors.add("Last name is required")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // ========== SYNC OPERATIONS ==========
    
    suspend fun getPendingSyncUsers(): List<UserEntity> = userDao.getPendingSyncUsers()
    
    suspend fun markAsSynced(userId: String): Boolean {
        return try {
            userDao.updateSyncStatus(userId, "synced")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun markSyncFailed(userId: String): Boolean {
        return try {
            userDao.updateSyncStatus(userId, "failed")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== DATA CLASSES ==========
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
    
    sealed class AuthResult {
        data class Success(val user: UserEntity) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
    
    sealed class CreateUserResult {
        data class Success(
            val user: UserEntity,
            val id: Long
        ) : CreateUserResult()
        
        data class Error(val message: String) : CreateUserResult()
    }
}