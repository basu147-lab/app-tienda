package com.puntofacil.ui.screens.reports

import com.puntofacil.data.entities.Customer
import com.puntofacil.data.entities.Product
import com.puntofacil.data.entities.Sale
import com.puntofacil.data.entities.SaleItem
import com.puntofacil.data.repositories.CustomerRepository
import com.puntofacil.data.repositories.ProductRepository
import com.puntofacil.data.repositories.SaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.Calendar

@ExperimentalCoroutinesApi
class ReportsViewModelTest {

    @Mock
    private lateinit var mockSaleRepository: SaleRepository
    @Mock
    private lateinit var mockProductRepository: ProductRepository
    @Mock
    private lateinit var mockCustomerRepository: CustomerRepository

    private lateinit var viewModel: ReportsViewModel

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

    private fun getDateRange(period: ReportPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        when (period) {
            ReportPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            }
            // Simplified for test, real implementation in VM
            else -> {calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)}
        }
        val startDate = calendar.timeInMillis
        return Pair(startDate, endDate)
    }

    @Test
    fun `initialState_reportsLoadedSuccessfully_emptyData`() = runTest {
        // Arrange: Mock all repository calls triggered by loadReports(ReportPeriod.TODAY)
        val (startDate, endDate) = getDateRange(ReportPeriod.TODAY)

        `when`(mockSaleRepository.getSalesByDateRange(anyLong(), anyLong())).thenReturn(emptyList<Sale>())
        `when`(mockSaleRepository.getAllSales()).thenReturn(emptyList<Sale>())
        `when`(mockSaleRepository.getSaleItems(anyString())).thenReturn(emptyList<SaleItem>())

        `when`(mockProductRepository.getProductById(anyString())).thenReturn(null) // No top product found
        `when`(mockProductRepository.getAllActiveProducts()).thenReturn(emptyList<Product>())
        `when`(mockProductRepository.getLowStockProducts()).thenReturn(emptyList<Product>())
        `when`(mockProductRepository.getOutOfStockProducts()).thenReturn(emptyList<Product>())

        `when`(mockCustomerRepository.getAllCustomers()).thenReturn(emptyList<Customer>())
        `when`(mockCustomerRepository.getCustomersByDateRange(anyLong(), anyLong())).thenReturn(emptyList<Customer>())

        // Act: Initialize the ViewModel
        viewModel = ReportsViewModel(mockSaleRepository, mockProductRepository, mockCustomerRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines in init complete

        // Assert: Check for default state after loading empty data
        val expectedUiState = ReportsUiState(
            isLoading = false,
            totalSales = 0.0,
            totalTransactions = 0,
            averageSale = 0.0,
            totalItemsSold = 0,
            topProduct = null,
            topProductSales = 0,
            totalProducts = 0,
            lowStockProducts = 0,
            outOfStockProducts = 0,
            totalCustomers = 0,
            newCustomers = 0,
            recentSales = emptyList(),
            errorMessage = null
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }
    
    @Test
    fun `initialState_repositoryError_errorMessageSet`() = runTest {
        // Arrange: Simulate an error during one of the initial repository calls
        val errorMessage = "Database connection error"
        `when`(mockSaleRepository.getSalesByDateRange(anyLong(), anyLong())).thenThrow(RuntimeException(errorMessage))

        // Other mocks can return default/empty values as they might not be reached or their failure isn't the focus
        `when`(mockSaleRepository.getAllSales()).thenReturn(emptyList<Sale>())
        `when`(mockSaleRepository.getSaleItems(anyString())).thenReturn(emptyList<SaleItem>())
        `when`(mockProductRepository.getProductById(anyString())).thenReturn(null)
        `when`(mockProductRepository.getAllActiveProducts()).thenReturn(emptyList<Product>())
        `when`(mockProductRepository.getLowStockProducts()).thenReturn(emptyList<Product>())
        `when`(mockProductRepository.getOutOfStockProducts()).thenReturn(emptyList<Product>())
        `when`(mockCustomerRepository.getAllCustomers()).thenReturn(emptyList<Customer>())
        `when`(mockCustomerRepository.getCustomersByDateRange(anyLong(), anyLong())).thenReturn(emptyList<Customer>())

        // Act
        viewModel = ReportsViewModel(mockSaleRepository, mockProductRepository, mockCustomerRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(false, actualState.isLoading)
        assertEquals("Error loading reports: $errorMessage", actualState.errorMessage)
    }
}
