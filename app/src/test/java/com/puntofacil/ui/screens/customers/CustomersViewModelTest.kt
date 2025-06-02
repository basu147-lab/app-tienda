package com.puntofacil.ui.screens.customers

import com.puntofacil.data.entities.Customer
import com.puntofacil.data.repositories.CustomerRepository
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
class CustomersViewModelTest {

    @Mock
    private lateinit var mockCustomerRepository: CustomerRepository

    private lateinit var viewModel: CustomersViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Define default behavior for repository methods called in init
        // viewModelScope.launch will execute tasks on the testDispatcher
        // We need to ensure that the suspending functions are called before advancing the dispatcher
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original one
    }

    @Test
    fun `initialState_customersLoadedAndDefaultValuesSet`() = runTest {
        // Arrange: Define mock responses for methods called during init
        val emptyCustomerList = emptyList<Customer>()
        `when`(mockCustomerRepository.getAllCustomers()).thenReturn(emptyCustomerList)
        `when`(mockCustomerRepository.getActiveCustomers()).thenReturn(emptyCustomerList)

        // Act: Initialize the ViewModel
        viewModel = CustomersViewModel(mockCustomerRepository)
        
        // Advance the dispatcher to allow coroutines in init to complete
        // This ensures loadCustomers() finishes
        testDispatcher.scheduler.advanceUntilIdle()


        // Assert: Check the state after init and loadCustomers
        val expectedUiState = CustomersUiState(
            isLoading = false, // Should be false after loading finishes
            customers = emptyCustomerList,
            filteredCustomers = emptyCustomerList,
            searchQuery = "",
            totalCustomers = 0,
            activeCustomers = 0,
            errorMessage = null
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }
}
