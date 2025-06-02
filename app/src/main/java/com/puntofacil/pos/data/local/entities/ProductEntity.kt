package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/**
 * Entidad de producto para la base de datos local
 * Basada en los esquemas analizados de los ejemplos
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplier_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["category_id"]),
        Index(value = ["supplier_id"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "barcode")
    val barcode: String? = null,
    
    @ColumnInfo(name = "price")
    val price: BigDecimal,
    
    @ColumnInfo(name = "cost")
    val cost: BigDecimal? = null,
    
    @ColumnInfo(name = "stock")
    val stock: Int = 0,
    
    @ColumnInfo(name = "min_stock")
    val minStock: Int = 0,
    
    @ColumnInfo(name = "max_stock")
    val maxStock: Int? = null,
    
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,
    
    @ColumnInfo(name = "supplier_id")
    val supplierId: String? = null,
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "has_variants")
    val hasVariants: Boolean = false,
    
    @ColumnInfo(name = "track_stock")
    val trackStock: Boolean = true,
    
    @ColumnInfo(name = "allow_negative_stock")
    val allowNegativeStock: Boolean = false,
    
    @ColumnInfo(name = "tax_rate")
    val taxRate: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "weight")
    val weight: BigDecimal? = null,
    
    @ColumnInfo(name = "weight_unit")
    val weightUnit: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending", // pending, synced, error
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
)