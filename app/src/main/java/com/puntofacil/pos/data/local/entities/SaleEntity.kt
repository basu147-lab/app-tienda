package com.puntofacil.pos.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/**
 * Entidad de venta para el sistema POS
 * Basada en los esquemas analizados de los ejemplos
 */
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["user_id"]),
        Index(value = ["sale_date"]),
        Index(value = ["status"]),
        Index(value = ["receipt_number"], unique = true)
    ]
)
data class SaleEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "receipt_number")
    val receiptNumber: String,
    
    @ColumnInfo(name = "customer_id")
    val customerId: String? = null,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "sale_date")
    val saleDate: Date = Date(),
    
    @ColumnInfo(name = "subtotal")
    val subtotal: BigDecimal,
    
    @ColumnInfo(name = "tax_amount")
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "discount_amount")
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: BigDecimal,
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // cash, card, mobile, mixed
    
    @ColumnInfo(name = "payment_status")
    val paymentStatus: String = "completed", // pending, completed, refunded
    
    @ColumnInfo(name = "status")
    val status: String = "completed", // pending, completed, cancelled, refunded
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "cash_received")
    val cashReceived: BigDecimal? = null,
    
    @ColumnInfo(name = "change_given")
    val changeGiven: BigDecimal? = null,
    
    @ColumnInfo(name = "card_last_four")
    val cardLastFour: String? = null,
    
    @ColumnInfo(name = "card_type")
    val cardType: String? = null,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null,
    
    @ColumnInfo(name = "is_refunded")
    val isRefunded: Boolean = false,
    
    @ColumnInfo(name = "refund_reason")
    val refundReason: String? = null,
    
    @ColumnInfo(name = "refunded_at")
    val refundedAt: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending",
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Date? = null
)