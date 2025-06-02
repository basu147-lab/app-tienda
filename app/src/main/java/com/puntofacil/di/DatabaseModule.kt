package com.puntofacil.di

import android.content.Context
import androidx.room.Room
import com.puntofacil.database.AppDatabase
import com.puntofacil.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "punto_facil_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
    
    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideSupplierDao(database: AppDatabase): SupplierDao {
        return database.supplierDao()
    }
    
    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao {
        return database.saleDao()
    }
    
    @Provides
    fun provideSaleItemDao(database: AppDatabase): SaleItemDao {
        return database.saleItemDao()
    }
    
    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}