package com.puntofacil.ui.screens.dashboard

import com.puntofacil.pos.data.local.entities.UserEntity
import com.puntofacil.repository.CustomerRepository
import com.puntofacil.repository.ProductRepository
import com.puntofacil.repository.SaleRepository
import com.puntofacil.repository.UserRepository
import com.puntofacil.data.models.Stats // Assuming Stats model for getTodayStats
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
class DashboardViewModelTest {

    @Mock
    private lateinit var mockUserRepository: UserRepository
    @Mock
    private lateinit var mockSaleRepository: SaleRepository
    @Mock
    private lateinit var mockProductRepository: ProductRepository
    @Mock
    private lateinit var mockCustomerRepository: CustomerRepository

    private lateinit var viewModel: DashboardViewModel

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
    fun `initialState_dashboardDataLoadedSuccessfully`() = runTest {
        // Arrange: Define mock responses for repository methods
        val mockAdminUser = UserEntity(id = "admin1", userName = "Admin User", pin = "1234", role = "ADMIN", isActive = true, createdAt = 0L, updatedAt = 0L)
        val mockUsers = listOf(mockAdminUser, UserEntity(id = "user1", userName = "Regular User", pin = "4321", role = "USER", isActive = true, createdAt = 0L, updatedAt = 0L))
        val mockTodayStats = Stats(total = 1500.75, count = 10, date = "") // Assuming Stats has total, count, date
        val mockProductCount = 120
        val mockCustomerCount = 75
        val mockLowStockCount = 5

        `when`(mockUserRepository.getAllActiveUsers()).thenReturn(mockUsers)
        `when`(mockSaleRepository.getTodayStats()).thenReturn(mockTodayStats)
        `when`(mockProductRepository.getActiveProductCount()).thenReturn(mockProductCount)
        `when`(mockCustomerRepository.getActiveCustomerCount()).thenReturn(mockCustomerCount)
        `when`(mockProductRepository.getLowStockProductCount()).thenReturn(mockLowStockCount)

        // Act: Initialize the ViewModel
        viewModel = DashboardViewModel(
            userRepository = mockUserRepository,
            saleRepository = mockSaleRepository,
            productRepository = mockProductRepository,
            customerRepository = mockCustomerRepository
        )
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines in init complete

        // Assert: Check the state after init and loadDashboardData
        val expectedUiState = DashboardUiState(
            isLoading = false,
            currentUser = mockAdminUser,
            todaySales = mockTodayStats.total,
            totalProducts = mockProductCount,
            totalCustomers = mockCustomerCount,
            lowStockProducts = mockLowStockCount,
            errorMessage = null
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `initialState_repositoryError_errorMessageSet`() = runTest {
        // Arrange: Setup one of the repository calls to throw an exception
        val errorMessage = "Database connection failed"
        `when`(mockUserRepository.getAllActiveUsers()).thenThrow(RuntimeException(errorMessage))
        // Other mocks can return default/empty values if needed for other parts of loadDashboardData
        `when`(mockSaleRepository.getTodayStats()).thenReturn(Stats(0.0, 0, ""))
        `when`(mockProductRepository.getActiveProductCount()).thenReturn(0)
        `when`(mockCustomerRepository.getActiveCustomerCount()).thenReturn(0)
        `when`(mockProductRepository.getLowStockProductCount()).thenReturn(0)


        // Act: Initialize the ViewModel
        viewModel = DashboardViewModel(
            userRepository = mockUserRepository,
            saleRepository = mockSaleRepository,
            productRepository = mockProductRepository,
            customerRepository = mockCustomerRepository
        )
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines in init complete

        // Assert: Check that isLoading is false and errorMessage is set
        val actualState = viewModel.uiState.value
        assertEquals(false, actualState.isLoading)
        assertEquals("Error al cargar datos del dashboard", actualState.errorMessage)
        //assertEquals(null, actualState.currentUser) // currentUser might be null
    }
}
