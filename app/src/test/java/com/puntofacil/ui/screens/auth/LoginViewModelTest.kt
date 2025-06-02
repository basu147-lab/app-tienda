package com.puntofacil.ui.screens.auth

import com.puntofacil.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @Mock
    private lateinit var mockUserRepository: UserRepository

    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(mockUserRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original one
    }

    @Test
    fun `initialState_defaultValuesAreSet`() {
        val expectedInitialState = LoginUiState(
            isLoading = false,
            isLoginSuccessful = false,
            errorMessage = null
        )
        assertEquals(expectedInitialState, viewModel.uiState.value)
    }
}
