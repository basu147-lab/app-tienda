package com.puntofacil.ui.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puntofacil.data.entities.Customer
import com.puntofacil.data.repositories.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomersUiState(
    val isLoading: Boolean = false,
    val customers: List<Customer> = emptyList(),
    val filteredCustomers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val customers = customerRepository.getAllCustomers()
                val activeCustomers = customerRepository.getActiveCustomers()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    customers = customers,
                    filteredCustomers = filterCustomers(customers, _uiState.value.searchQuery),
                    totalCustomers = customers.size,
                    activeCustomers = activeCustomers.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading customers: ${e.message}"
                )
            }
        }
    }

    fun searchCustomers(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredCustomers = filterCustomers(_uiState.value.customers, query)
        )
    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                customerRepository.createCustomer(customer)
                loadCustomers() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error adding customer: ${e.message}"
                )
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                customerRepository.updateCustomer(customer)
                loadCustomers() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating customer: ${e.message}"
                )
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                customerRepository.deleteCustomer(customerId)
                loadCustomers() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting customer: ${e.message}"
                )
            }
        }
    }

    fun activateCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    val updatedCustomer = customer.copy(isActive = true, updatedAt = System.currentTimeMillis())
                    customerRepository.updateCustomer(updatedCustomer)
                    loadCustomers() // Refresh the list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error activating customer: ${e.message}"
                )
            }
        }
    }

    fun deactivateCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            try {
                val customer = customerRepository.getCustomerById(customerId)
                if (customer != null) {
                    val updatedCustomer = customer.copy(isActive = false, updatedAt = System.currentTimeMillis())
                    customerRepository.updateCustomer(updatedCustomer)
                    loadCustomers() // Refresh the list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deactivating customer: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun filterCustomers(customers: List<Customer>, searchQuery: String): List<Customer> {
        if (searchQuery.isBlank()) {
            return customers
        }
        
        return customers.filter {
            it.firstName.contains(searchQuery, ignoreCase = true) ||
            it.lastName.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            "${it.firstName} ${it.lastName}".contains(searchQuery, ignoreCase = true)
        }
    }

    fun refreshData() {
        loadCustomers()
    }
}