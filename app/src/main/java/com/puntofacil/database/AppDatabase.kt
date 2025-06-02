package com.puntofacil.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.puntofacil.pos.data.local.dao.*
import com.puntofacil.pos.data.local.entities.*
import com.puntofacil.pos.data.local.converters.Converters

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        CustomerEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "punto_facil_database"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}