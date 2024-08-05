package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.MY_LOCATION
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_LIST
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentLiveNavigationTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showLiveNavigationTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val edtSearch = waitForView(allOf(withId(R.id.edt_search_places), isDisplayed()))
            edtSearch?.perform(click(), replaceText(TEST_WORD_4))

            val rvRecyclerView = waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            rvRecyclerView?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            // txtTime
            waitForView(
                allOf(
                    withId(R.id.tv_direction_distance),
                    isDisplayed(),
                ),
            )

            val btnDirection = waitForView(
                allOf(
                    withId(R.id.btn_direction),
                    isDisplayed(),
                ),
            )
            btnDirection?.perform(click())

            waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))

            var shouldSetMyLocation = false
            getInstrumentation().runOnMainSync {
                val edtSearchDirection = mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                if (edtSearchDirection.text.isNullOrBlank() || edtSearchDirection.text?.trim() != MY_LOCATION) {
                    shouldSetMyLocation = true
                }
            }

            if (shouldSetMyLocation) {
                getInstrumentation().runOnMainSync {
                    val edtSearchDirection = mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                    edtSearchDirection.performClick()
                }

                val tvGetLocation = waitForView(allOf(withText(R.string.label_my_location), isDisplayed()))
                tvGetLocation?.perform(click())
            }

            val btnCarGo = waitForView(
                allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO),
                    ),
                    isDisplayed(),
                ),
            )
            btnCarGo?.perform(click())

            Espresso.closeSoftKeyboard()

            // navListView
            waitForView(allOf(withId(R.id.rv_navigation_list), isDisplayed(), hasMinimumChildCount(1)))
        } catch (e: Exception) {
            failTest(136, e)
            Assert.fail(TEST_FAILED_LIST)
        }
    }
}
