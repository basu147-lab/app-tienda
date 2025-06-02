package com.puntofacil.repository

import com.puntofacil.database.dao.CategoryDao
import com.puntofacil.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    // ========== CATEGORY RETRIEVAL ==========
    
    fun getAllActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
    
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: String): CategoryEntity? = categoryDao.getCategoryById(id)
    
    suspend fun getCategoryByName(name: String): CategoryEntity? = categoryDao.getCategoryByName(name)
    
    fun searchCategories(query: String): Flow<List<CategoryEntity>> = 
        categoryDao.searchCategories(query)
    
    fun getCategoriesWithProductCount(): Flow<List<CategoryWithProductCount>> = 
        categoryDao.getCategoriesWithProductCount().let { flow ->
            kotlinx.coroutines.flow.map(flow) { categories ->
                categories.map { category ->
                    CategoryWithProductCount(
                        category = category.category,
                        productCount = category.productCount
                    )
                }
            }
        }
    
    // ========== CATEGORY STATISTICS ==========
    
    suspend fun getActiveCategoryCount(): Int = categoryDao.getActiveCategoryCount()
    
    // ========== CATEGORY MANAGEMENT ==========
    
    suspend fun createCategory(
        name: String,
        description: String? = null,
        color: String? = null,
        icon: String? = null,
        sortOrder: Int? = null
    ): Long {
        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            color = color,
            icon = icon,
            isActive = true,
            sortOrder = sortOrder ?: getNextSortOrder(),
            createdAt = Date(),
            updatedAt = Date(),
            syncStatus = "pending",
            lastSyncAt = null
        )
        return categoryDao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: CategoryEntity): Boolean {
        return try {
            val updatedCategory = category.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
            categoryDao.updateCategory(updatedCategory)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteCategory(categoryId: String): Boolean {
        return try {
            categoryDao.deleteCategoryById(categoryId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun activateCategory(categoryId: String): Boolean {
        return try {
            categoryDao.activateCategory(categoryId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deactivateCategory(categoryId: String): Boolean {
        return try {
            categoryDao.deactivateCategory(categoryId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== CATEGORY ORDERING ==========
    
    suspend fun reorderCategories(categoryIds: List<String>): Boolean {
        return try {
            categoryDao.reorderCategories(categoryIds)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateSortOrder(categoryId: String, sortOrder: Int): Boolean {
        return try {
            categoryDao.updateSortOrder(categoryId, sortOrder)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun getNextSortOrder(): Int {
        val categories = categoryDao.getAllCategories()
        return categories.value?.maxOfOrNull { it.sortOrder }?.plus(1) ?: 1
    }
    
    // ========== VALIDATION ==========
    
    suspend fun validateCategoryData(
        name: String,
        excludeCategoryId: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Category name is required")
        } else {
            val existingCategory = getCategoryByName(name)
            if (existingCategory != null && existingCategory.id != excludeCategoryId) {
                errors.add("Category name already exists")
            }
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    // ========== SYNC OPERATIONS ==========
    
    suspend fun getPendingSyncCategories(): List<CategoryEntity> = 
        categoryDao.getPendingSyncCategories()
    
    suspend fun markAsSynced(categoryId: String): Boolean {
        return try {
            categoryDao.updateSyncStatus(categoryId, "synced")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun markSyncFailed(categoryId: String): Boolean {
        return try {
            categoryDao.updateSyncStatus(categoryId, "failed")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== BULK OPERATIONS ==========
    
    suspend fun bulkInsertCategories(categories: List<CategoryEntity>): List<Long> {
        return categoryDao.insertCategories(categories)
    }
    
    suspend fun bulkUpdateCategories(categories: List<CategoryEntity>) {
        val updatedCategories = categories.map { category ->
            category.copy(
                updatedAt = Date(),
                syncStatus = "pending"
            )
        }
        categoryDao.updateCategories(updatedCategories)
    }
    
    suspend fun bulkDeactivateCategories(categoryIds: List<String>): Boolean {
        return try {
            categoryIds.forEach { categoryId ->
                categoryDao.deactivateCategory(categoryId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ========== CATEGORY COLORS AND ICONS ==========
    
    fun getDefaultColors(): List<String> = listOf(
        "#FF6B6B", // Red
        "#4ECDC4", // Teal
        "#45B7D1", // Blue
        "#96CEB4", // Green
        "#FFEAA7", // Yellow
        "#DDA0DD", // Plum
        "#98D8C8", // Mint
        "#F7DC6F", // Light Yellow
        "#BB8FCE", // Light Purple
        "#85C1E9", // Light Blue
        "#F8C471", // Orange
        "#82E0AA"  // Light Green
    )
    
    fun getDefaultIcons(): List<String> = listOf(
        "shopping_cart",
        "local_grocery_store",
        "restaurant",
        "local_cafe",
        "local_bar",
        "cake",
        "fastfood",
        "local_pizza",
        "icecream",
        "local_dining",
        "store",
        "inventory",
        "category",
        "label",
        "bookmark",
        "folder",
        "archive",
        "inbox",
        "star",
        "favorite"
    )
    
    // ========== DATA CLASSES ==========
    
    data class CategoryWithProductCount(
        val category: CategoryEntity,
        val productCount: Int
    )
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}