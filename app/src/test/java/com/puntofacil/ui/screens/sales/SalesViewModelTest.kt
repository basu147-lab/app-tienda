package com.puntofacil.ui.screens.sales

import com.puntofacil.pos.data.local.entities.ProductEntity
import com.puntofacil.repository.ProductRepository
import com.puntofacil.repository.SaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SalesViewModelTest {

    @Mock
    private lateinit var mockProductRepository: ProductRepository
    @Mock
    private lateinit var mockSaleRepository: SaleRepository

    private lateinit var viewModel: SalesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialState_initialProductsLoaded`() = runTest {
        // Arrange
        val mockProducts = List(25) { i -> // Create 25 mock products
            ProductEntity(
                id = i.toLong(),
                name = "Product $i",
                description = "Description $i",
                salePrice = 10.0 * (i + 1),
                purchasePrice = 5.0 * (i + 1),
                stock = 10 + i,
                minStock = 5,
                barcode = "BC-$i",
                sku = "SKU-$i",
                categoryId = 1L,
                supplierId = 1L,
                isActive = true,
                imageUrl = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        val expectedInitialSearchResults = mockProducts.take(20)
        `when`(mockProductRepository.getAllActiveProducts()).thenReturn(mockProducts)

        // Act
        viewModel = SalesViewModel(mockProductRepository, mockSaleRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines in init complete

        // Assert
        val expectedUiState = SalesUiState(
            searchResults = expectedInitialSearchResults,
            saleItems = emptyList(),
            total = 0.0,
            isProcessing = false,
            message = null,
            isLoading = false // isLoading is not explicitly set to true in init's happy path
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `initialState_productLoadingError_messageSet`() = runTest {
        // Arrange
        val errorMessage = "Failed to load products"
        `when`(mockProductRepository.getAllActiveProducts()).thenThrow(RuntimeException(errorMessage))

        // Act
        viewModel = SalesViewModel(mockProductRepository, mockSaleRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(emptyList<ProductEntity>(), actualState.searchResults)
        assertEquals("Error al cargar productos", actualState.message)
        assertEquals(false, actualState.isLoading)
    }
}
