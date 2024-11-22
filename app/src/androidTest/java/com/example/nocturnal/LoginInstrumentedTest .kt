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
class LoginInstrumentedTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSuccessfulLogin() {
        // Enter valid email
        onView(withId(R.id.username))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Enter valid password
        onView(withId(R.id.password))
            .perform(typeText("password123"), closeSoftKeyboard())

        // Click the login button
        onView(withId(R.id.login)).perform(click())

        // Verify navigation to CameraActivity by checking toolbar visibility
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }
}
