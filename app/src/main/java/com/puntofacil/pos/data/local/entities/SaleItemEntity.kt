package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/**
 * Entidad de elemento de venta (productos vendidos en cada transacción)
 */
@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["sale_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["sale_id"]),
        Index(value = ["product_id"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "sale_id")
    val saleId: String,
    
    @ColumnInfo(name = "product_id")
    val productId: String,
    
    @ColumnInfo(name = "product_name")
    val productName: String, // Snapshot del nombre al momento de la venta
    
    @ColumnInfo(name = "product_barcode")
    val productBarcode: String? = null,
    
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    
    @ColumnInfo(name = "unit_price")
    val unitPrice: BigDecimal, // Precio al momento de la venta
    
    @ColumnInfo(name = "unit_cost")
    val unitCost: BigDecimal? = null, // Costo al momento de la venta
    
    @ColumnInfo(name = "discount_amount")
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "discount_percentage")
    val discountPercentage: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "tax_rate")
    val taxRate: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "tax_amount")
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "line_total")
    val lineTotal: BigDecimal, // (quantity * unit_price) - discount + tax
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "is_refunded")
    val isRefunded: Boolean = false,
    
    @ColumnInfo(name = "refunded_quantity")
    val refundedQuantity: Int = 0,
    
    @ColumnInfo(name = "refund_reason")
    val refundReason: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending",
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
)