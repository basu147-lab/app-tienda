package com.puntofacil.di

import com.puntofacil.database.dao.*
import com.puntofacil.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao
    ): ProductRepository {
        return ProductRepository(productDao)
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
    
    @Provides
    @Singleton
    fun provideCustomerRepository(
        customerDao: CustomerDao
    ): CustomerRepository {
        return CustomerRepository(customerDao)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }
    
    @Provides
    @Singleton
    fun provideSaleRepository(
        saleDao: SaleDao,
        saleItemDao: SaleItemDao,
        productDao: ProductDao
    ): SaleRepository {
        return SaleRepository(saleDao, saleItemDao, productDao)
    }
}