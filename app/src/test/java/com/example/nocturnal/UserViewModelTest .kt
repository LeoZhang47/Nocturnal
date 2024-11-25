package com.example.nocturnal

import com.example.nocturnal.data.FirestoreRepository
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.junit.Assert.assertEquals


@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockRepository: FirestoreRepository
    private lateinit var userViewModel: UserViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set Main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Mock FirebaseAuth and FirestoreRepository
        mockAuth = mock()
        mockRepository = mock()

        // Inject mocks into UserViewModel
        userViewModel = UserViewModel(auth = mockAuth, repository = mockRepository)
    }

    @After
    fun tearDown() {
        // Reset Main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `getUsername returns username successfully`() = runTest {
        println("Starting test: getUsername returns username successfully")

        // Arrange
        val uid = "7fUxx8FALyReb1fgnti536TSzzr1"
        val expectedUsername = "test_user"
        var username: String? = null

        println("Mocking repository behavior...")
        whenever(mockRepository.getUsername(uid)).thenReturn(expectedUsername)

        // Act
        println("Calling getUsername in UserViewModel...")
        userViewModel.getUsername(uid) { name ->
            println("Test: Callback received with username: $name")
            username = name
        }

        println("Advancing coroutine dispatcher...")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        println("Asserting the result...")
        assertEquals(expectedUsername, username)
        println("Test passed!")
    }


    @Test
    fun `getUsername handles exception and returns null`() = runTest {
        println("Starting test: getUsername handles exception and returns null")

        // Arrange
        val uid = "invalid_uid"
        val exceptionMessage = "User not found"
        var username: String? = null

        println("Mocking repository to throw an exception...")
        whenever(mockRepository.getUsername(uid)).thenThrow(RuntimeException(exceptionMessage))

        // Act
        println("Calling getUsername in UserViewModel...")
        userViewModel.getUsername(uid) { name ->
            println("Test: Callback received with username: $name")
            username = name
        }

        println("Advancing coroutine dispatcher...")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        println("Asserting that username is null...")
        assertEquals(null, username)
        println("Test passed!")
    }

}
