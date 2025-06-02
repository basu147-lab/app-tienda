package com.puntofacil.ui.screens.settings

import com.puntofacil.data.repositories.CustomerRepository
import com.puntofacil.data.repositories.ProductRepository
import com.puntofacil.data.repositories.SaleRepository
import com.puntofacil.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @Mock
    private lateinit var mockUserRepository: UserRepository
    @Mock
    private lateinit var mockProductRepository: ProductRepository
    @Mock
    private lateinit var mockCustomerRepository: CustomerRepository
    @Mock
    private lateinit var mockSaleRepository: SaleRepository

    private lateinit var viewModel: SettingsViewModel

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
    fun `initialState_defaultValuesAreSet`() = runTest {
        // Arrange: No specific arrangements needed as init block is empty

        // Act: Initialize the ViewModel
        viewModel = SettingsViewModel(
            userRepository = mockUserRepository,
            productRepository = mockProductRepository,
            customerRepository = mockCustomerRepository,
            saleRepository = mockSaleRepository
        )
        // No need to advance dispatcher as init is empty and no coroutines are launched by default

        // Assert
        val expectedUiState = SettingsUiState(
            isLoading = false,
            backupInProgress = false,
            exportInProgress = false,
            successMessage = null,
            errorMessage = null
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }
}
