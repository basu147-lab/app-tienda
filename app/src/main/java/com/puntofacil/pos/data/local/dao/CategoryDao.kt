package com.puntofacil.pos.data.local.dao

import androidx.room.*
import com.puntofacil.pos.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de categorías en la base de datos
 */
@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories ORDER BY sort_order ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): CategoryEntity?
    
    @Query("""
        SELECT * FROM categories 
        WHERE name LIKE '%' || :query || '%' 
        AND is_active = 1 
        ORDER BY name ASC
    """)
    fun searchCategories(query: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT COUNT(*) FROM categories WHERE is_active = 1")
    fun getActiveCategoryCount(): Flow<Int>
    
    @Query("""
        SELECT c.*, COUNT(p.id) as product_count 
        FROM categories c 
        LEFT JOIN products p ON c.id = p.category_id AND p.is_active = 1
        WHERE c.is_active = 1
        GROUP BY c.id
        ORDER BY c.sort_order ASC, c.name ASC
    """)
    suspend fun getCategoriesWithProductCount(): List<CategoryWithProductCount>
    
    @Query("UPDATE categories SET is_active = 0 WHERE id = :categoryId")
    suspend fun deactivateCategory(categoryId: String)
    
    @Query("UPDATE categories SET is_active = 1 WHERE id = :categoryId")
    suspend fun activateCategory(categoryId: String)
    
    @Query("UPDATE categories SET sort_order = :sortOrder WHERE id = :categoryId")
    suspend fun updateSortOrder(categoryId: String, sortOrder: Int)
    
    @Query("UPDATE categories SET sync_status = :status WHERE id = :categoryId")
    suspend fun updateSyncStatus(categoryId: String, status: String)
    
    @Query("SELECT * FROM categories WHERE sync_status = 'pending'")
    suspend fun getPendingSyncCategories(): List<CategoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Transaction
    suspend fun upsertCategory(category: CategoryEntity) {
        val existingCategory = getCategoryById(category.id)
        if (existingCategory != null) {
            updateCategory(category.copy(createdAt = existingCategory.createdAt))
        } else {
            insertCategory(category)
        }
    }
    
    @Transaction
    suspend fun reorderCategories(categoryIds: List<String>) {
        categoryIds.forEachIndexed { index, categoryId ->
            updateSortOrder(categoryId, index)
        }
    }
}

/**
 * Data class para categorías con conteo de productos
 */
data class CategoryWithProductCount(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val is_active: Boolean,
    val sort_order: Int,
    val product_count: Int
)