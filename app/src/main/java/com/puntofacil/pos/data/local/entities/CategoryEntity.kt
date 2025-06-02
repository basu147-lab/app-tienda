package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad de categoría para organizar productos
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "color")
    val color: String? = null,
    
    @ColumnInfo(name = "icon")
    val icon: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending",
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
)