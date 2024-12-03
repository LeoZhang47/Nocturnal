package com.example.nocturnal

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nocturnal.ui.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.GrantPermissionRule


@RunWith(AndroidJUnit4::class)
class SignupInstrumentedTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSuccessfulSignup() {
        onView(withId(R.id.switch_to_signup)).perform(click())
        // Enter valid email
        onView(withId(R.id.email))
            .perform(typeText("signupTest@example.com"), closeSoftKeyboard())

        // Enter valid username
        onView(withId(R.id.username))
            .perform(typeText("Signup Test"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.password))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Confirm valid password
        onView(withId(R.id.confirm_password))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click the signup button
        onView(withId(R.id.register)).perform(click())

        // Verify navigation to CameraActivity by checking toolbar visibility
        Thread.sleep(2000) // Wait for 2 seconds
        onView(withId(R.id.picture_button))
            .check(matches(isDisplayed()))
    }
}
