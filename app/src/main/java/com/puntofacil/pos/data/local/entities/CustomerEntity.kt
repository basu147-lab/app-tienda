package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/**
 * Entidad de cliente para el sistema POS
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["email"]),
        Index(value = ["phone"]),
        Index(value = ["document_number"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "first_name")
    val firstName: String,
    
    @ColumnInfo(name = "last_name")
    val lastName: String? = null,
    
    @ColumnInfo(name = "email")
    val email: String? = null,
    
    @ColumnInfo(name = "phone")
    val phone: String? = null,
    
    @ColumnInfo(name = "document_type")
    val documentType: String? = null, // DNI, RUC, Passport, etc.
    
    @ColumnInfo(name = "document_number")
    val documentNumber: String? = null,
    
    @ColumnInfo(name = "address")
    val address: String? = null,
    
    @ColumnInfo(name = "city")
    val city: String? = null,
    
    @ColumnInfo(name = "state")
    val state: String? = null,
    
    @ColumnInfo(name = "postal_code")
    val postalCode: String? = null,
    
    @ColumnInfo(name = "country")
    val country: String? = null,
    
    @ColumnInfo(name = "birth_date")
    val birthDate: Date? = null,
    
    @ColumnInfo(name = "gender")
    val gender: String? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "loyalty_points")
    val loyaltyPoints: Int = 0,
    
    @ColumnInfo(name = "total_spent")
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "total_visits")
    val totalVisits: Int = 0,
    
    @ColumnInfo(name = "last_visit")
    val lastVisit: Date? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "marketing_consent")
    val marketingConsent: Boolean = false,
    
    @ColumnInfo(name = "preferred_contact_method")
    val preferredContactMethod: String? = null, // email, phone, sms
    
    @ColumnInfo(name = "customer_group")
    val customerGroup: String? = null, // VIP, Regular, New, etc.
    
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
        get() = if (lastName != null) "$firstName $lastName" else firstName
}