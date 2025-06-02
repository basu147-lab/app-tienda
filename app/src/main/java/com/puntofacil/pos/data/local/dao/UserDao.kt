package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO para operaciones de usuarios en la base de datos
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY first_name ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users ORDER BY first_name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username AND is_active = 1")
    suspend fun getActiveUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email AND is_active = 1")
    suspend fun getActiveUserByEmail(email: String): UserEntity?
    
    @Query("""
        SELECT * FROM users 
        WHERE (first_name LIKE '%' || :query || '%' 
               OR last_name LIKE '%' || :query || '%' 
               OR username LIKE '%' || :query || '%' 
               OR email LIKE '%' || :query || '%') 
        AND is_active = 1 
        ORDER BY first_name ASC
    """)
    fun searchUsers(query: String): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE role = :role AND is_active = 1 ORDER BY first_name ASC")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>
    
    @Query("SELECT COUNT(*) FROM users WHERE is_active = 1")
    fun getActiveUserCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM users WHERE role = :role AND is_active = 1")
    fun getUserCountByRole(role: String): Flow<Int>
    
    @Query("""
        SELECT * FROM users 
        WHERE last_login < :date AND is_active = 1 
        ORDER BY last_login ASC
    """)
    fun getInactiveUsers(date: Date): Flow<List<UserEntity>>
    
    @Query("""
        SELECT * FROM users 
        WHERE locked_until > :currentDate AND is_active = 1
    """)
    fun getLockedUsers(currentDate: Date): Flow<List<UserEntity>>
    
    @Query("""
        UPDATE users 
        SET last_login = :loginDate, login_attempts = 0, locked_until = NULL 
        WHERE id = :userId
    """)
    suspend fun updateLastLogin(userId: String, loginDate: Date)
    
    @Query("""
        UPDATE users 
        SET login_attempts = login_attempts + 1 
        WHERE id = :userId
    """)
    suspend fun incrementLoginAttempts(userId: String)
    
    @Query("""
        UPDATE users 
        SET locked_until = :lockUntil, login_attempts = :attempts 
        WHERE id = :userId
    """)
    suspend fun lockUser(userId: String, lockUntil: Date, attempts: Int)
    
    @Query("""
        UPDATE users 
        SET locked_until = NULL, login_attempts = 0 
        WHERE id = :userId
    """)
    suspend fun unlockUser(userId: String)
    
    @Query("""
        UPDATE users 
        SET password_hash = :newPasswordHash, password_changed_at = :changeDate 
        WHERE id = :userId
    """)
    suspend fun updatePassword(userId: String, newPasswordHash: String, changeDate: Date)
    
    @Query("""
        UPDATE users 
        SET two_factor_enabled = :enabled, two_factor_secret = :secret 
        WHERE id = :userId
    """)
    suspend fun updateTwoFactorAuth(userId: String, enabled: Boolean, secret: String?)
    
    @Query("UPDATE users SET is_active = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: String)
    
    @Query("UPDATE users SET is_active = 1 WHERE id = :userId")
    suspend fun activateUser(userId: String)
    
    @Query("UPDATE users SET is_verified = 1 WHERE id = :userId")
    suspend fun verifyUser(userId: String)
    
    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: String)
    
    @Query("UPDATE users SET permissions = :permissions WHERE id = :userId")
    suspend fun updateUserPermissions(userId: String, permissions: String)
    
    @Query("UPDATE users SET sync_status = :status WHERE id = :userId")
    suspend fun updateSyncStatus(userId: String, status: String)
    
    @Query("SELECT * FROM users WHERE sync_status = 'pending'")
    suspend fun getPendingSyncUsers(): List<UserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    @Transaction
    suspend fun upsertUser(user: UserEntity) {
        val existingUser = getUserById(user.id)
        if (existingUser != null) {
            updateUser(user.copy(createdAt = existingUser.createdAt))
        } else {
            insertUser(user)
        }
    }
    
    @Transaction
    suspend fun authenticateUser(username: String, passwordHash: String): UserEntity? {
        val user = getActiveUserByUsername(username) ?: getActiveUserByEmail(username)
        return if (user != null && user.passwordHash == passwordHash && !user.isLocked) {
            updateLastLogin(user.id, Date())
            user
        } else {
            user?.let { incrementLoginAttempts(it.id) }
            null
        }
    }
}