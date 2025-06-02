package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad de proveedor para gestión de inventario
 */
@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["email"]),
        Index(value = ["phone"])
    ]
)
data class SupplierEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "contact_person")
    val contactPerson: String? = null,
    
    @ColumnInfo(name = "email")
    val email: String? = null,
    
    @ColumnInfo(name = "phone")
    val phone: String? = null,
    
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
    
    @ColumnInfo(name = "tax_id")
    val taxId: String? = null,
    
    @ColumnInfo(name = "payment_terms")
    val paymentTerms: String? = null,
    
    @ColumnInfo(name = "credit_limit")
    val creditLimit: String? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending",
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
)