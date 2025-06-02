package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad de usuario para el sistema de autenticación
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    @ColumnInfo(name = "first_name")
    val firstName: String,
    
    @ColumnInfo(name = "last_name")
    val lastName: String,
    
    @ColumnInfo(name = "role")
    val role: String, // admin, manager, cashier, employee
    
    @ColumnInfo(name = "permissions")
    val permissions: String, // JSON string with permissions
    
    @ColumnInfo(name = "phone")
    val phone: String? = null,
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "last_login")
    val lastLogin: Date? = null,
    
    @ColumnInfo(name = "login_attempts")
    val loginAttempts: Int = 0,
    
    @ColumnInfo(name = "locked_until")
    val lockedUntil: Date? = null,
    
    @ColumnInfo(name = "password_changed_at")
    val passwordChangedAt: Date = Date(),
    
    @ColumnInfo(name = "security_question")
    val securityQuestion: String? = null,
    
    @ColumnInfo(name = "security_answer_hash")
    val securityAnswerHash: String? = null,
    
    @ColumnInfo(name = "two_factor_enabled")
    val twoFactorEnabled: Boolean = false,
    
    @ColumnInfo(name = "two_factor_secret")
    val twoFactorSecret: String? = null,
    
    @ColumnInfo(name = "backup_codes")
    val backupCodes: String? = null, // JSON array of backup codes
    
    @ColumnInfo(name = "preferences")
    val preferences: String? = null, // JSON string with user preferences
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending",
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val isLocked: Boolean
        get() = lockedUntil != null && lockedUntil!! > Date()
}