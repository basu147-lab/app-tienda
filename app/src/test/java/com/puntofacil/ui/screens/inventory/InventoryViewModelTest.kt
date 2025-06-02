package com.puntofacil.ui.screens.inventory

import com.puntofacil.data.entities.Product
import com.puntofacil.data.repositories.ProductRepository
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
class InventoryViewModelTest {

    @Mock
    private lateinit var mockProductRepository: ProductRepository

    private lateinit var viewModel: InventoryViewModel

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
    fun `initialState_productsLoadedSuccessfully`() = runTest {
        // Arrange: Define mock responses
        val mockAllProducts = listOf(
            Product(id = "1", name = "Product 1", stock = 10, minStock = 5, price = 10.0, cost = 5.0, barcode = "123", sku = "P1"),
            Product(id = "2", name = "Product 2", stock = 3, minStock = 5, price = 20.0, cost = 10.0, barcode = "456", sku = "P2"), // Low stock
            Product(id = "3", name = "Product 3", stock = 0, minStock = 5, price = 30.0, cost = 15.0, barcode = "789", sku = "P3")  // Out of stock
        )
        val mockLowStockProducts = mockAllProducts.filter { it.stock <= it.minStock && it.stock > 0 }
        val mockOutOfStockProducts = mockAllProducts.filter { it.stock <= 0 }

        `when`(mockProductRepository.getAllActiveProducts()).thenReturn(mockAllProducts)
        `when`(mockProductRepository.getLowStockProducts()).thenReturn(mockLowStockProducts)
        `when`(mockProductRepository.getOutOfStockProducts()).thenReturn(mockOutOfStockProducts)

        // Act: Initialize the ViewModel
        viewModel = InventoryViewModel(mockProductRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines in init complete

        // Assert
        val expectedUiState = InventoryUiState(
            isLoading = false,
            products = mockAllProducts,
            filteredProducts = mockAllProducts, // Initially, filter is ALL and search is empty
            searchQuery = "",
            selectedFilter = ProductFilter.ALL,
            totalProducts = mockAllProducts.size,
            lowStockCount = mockLowStockProducts.size,
            outOfStockCount = mockOutOfStockProducts.size,
            errorMessage = null
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `initialState_repositoryError_errorMessageSet`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        `when`(mockProductRepository.getAllActiveProducts()).thenThrow(RuntimeException(errorMessage))
        // Provide defaults for other calls in case they are reached before error is fully processed,
        // though in this ViewModel's loadProducts, the first error will likely short-circuit.
        `when`(mockProductRepository.getLowStockProducts()).thenReturn(emptyList())
        `when`(mockProductRepository.getOutOfStockProducts()).thenReturn(emptyList())


        // Act
        viewModel = InventoryViewModel(mockProductRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(false, actualState.isLoading)
        assertEquals("Error loading products: $errorMessage", actualState.errorMessage)
        assertEquals(emptyList(), actualState.products)
        assertEquals(0, actualState.totalProducts)
    }
}
