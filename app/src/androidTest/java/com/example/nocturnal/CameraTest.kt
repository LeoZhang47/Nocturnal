package com.example.nocturnal

import android.accounts.AccountManager
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.nocturnal.ui.activity.CameraActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CameraTest {
    @get:Rule
    val grantLocation: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val grantCamera: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @get:Rule
    val activityRule = ActivityScenarioRule(CameraActivity::class.java)

    @Mock
    lateinit var firebaseAuth: FirebaseAuth

    @Mock
    lateinit var firebaseUser: FirebaseUser

    private fun createDummyImageFileWithUri(context: Context, fileName: String): Pair<File, Uri> {
        // Create a dummy file in the same directory as your app's real files
        val dummyFile = File(context.filesDir, fileName)

        context.resources.openRawResource(R.drawable.defaultimage).use { inputStream ->
            FileOutputStream(dummyFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Generate the URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            dummyFile
        )

        return Pair(dummyFile, uri)
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
        `when`(firebaseUser.uid).thenReturn("testUserID")
        `when`(firebaseUser.email).thenReturn("testuser@example.com")

        Intents.init()

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create a dummy file with the same URI pattern
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "IMG_${timestamp}.JPG"
        val (dummyFile, dummyUri) = createDummyImageFileWithUri(context, fileName)

        // Stub the camera intent to return the dummy URI
        val resultData = Intent().apply {
            data = dummyUri
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Stub the ACTION_IMAGE_CAPTURE intent
        intending(hasAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)

    }

    @After
    fun tearDown() {
        Intents.release()
    }

//    private fun setLocationEthyl() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        val provider = LocationManager.GPS_PROVIDER
//        locationManager.addTestProvider(provider, false, false, false, false, false, false, false, 0, 5)
//        locationManager.setTestProviderEnabled(provider, true)
//
//        val location = Location(provider).apply{
//            latitude = 39.997713522335644
//            longitude = -83.00699141155631
//            accuracy = 1.0f
//            time = System.currentTimeMillis()
//        }
//
//        locationManager.setTestProviderLocation(provider, location)
//
//    }

//    @Test
//    fun checkLocationEthyl() {
//        //setLocationEthyl()
//        lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//
//        val expectedLatitude = 39.997713522335644
//        val expectedLongitude = -83.00699141155631
//
//        val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
//        locationTask.addOnSuccessListener { location ->
//            if (location != null) {
//                // Check that the location matches the expected latitude and longitude
//                assertEquals("Latitude does not match", expectedLatitude, location.latitude, 0.0001)
//                assertEquals(
//                    "Longitude does not match",
//                    expectedLongitude,
//                    location.longitude,
//                    0.0001
//                )
//            } else {
//                // Handle the case where location is null (e.g., permission issues)
//                throw AssertionError("Location is null")
//            }
//        }
//    }

    @Test
    fun checkImageFragment() {
        onView(withId(R.id.picture_button)).perform(click())

        onView(withId(R.id.post_button)).check(matches(isDisplayed()))
        onView(withId(R.id.cancel_button)).check(matches(isDisplayed()))
    }

}
