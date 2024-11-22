package com.example.nocturnal.data.model.viewmodel

import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockRepository: FirestoreRepository
    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        // Mock FirebaseAuth and FirestoreRepository
        mockAuth = mock()
        mockRepository = mock()

        // Inject mocks into UserViewModel
        userViewModel = UserViewModel(auth = mockAuth, repository = mockRepository)
    }

    @Test
    fun `getUsername returns username successfully`() = runBlockingTest {
        // Arrange
        val uid = "7fUxx8FALyReb1fgnti536TSzzr1"
        val expectedUsername = "test"

        // Mock FirestoreRepository behavior
        doAnswer { invocation ->
            val onSuccess = invocation.arguments[1] as (String) -> Unit
            onSuccess(expectedUsername) // Simulate success callback
            null // Return null for Unit functions
        }.`when`(mockRepository).getUsername(eq(uid), any(), any())

        // Act
        val usernameFlow = userViewModel.getUsername(uid)
        val username = usernameFlow.first() // Collect the first emitted value

        // Assert
        assertEquals(expectedUsername, username)
    }

    @Test
    fun `getUsername handles error gracefully`() = runBlockingTest {
        // Arrange
        val uid = "test_uid"
        val expectedError = "User not found"

        // Mock FirestoreRepository behavior
        doAnswer { invocation ->
            val onFailure = invocation.arguments[2] as (Exception) -> Unit
            onFailure(Exception(expectedError)) // Simulate error callback
            null // Return null for Unit functions
        }.`when`(mockRepository).getUsername(eq(uid), any(), any())

        // Act
        val usernameFlow = userViewModel.getUsername(uid)
        val username = usernameFlow.first() // Collect the first emitted value

        // Assert
        assertEquals("Error: $expectedError", username)
    }
}
