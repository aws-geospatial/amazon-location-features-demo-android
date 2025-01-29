package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_TIME_NOT_VISIBLE
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchAddressExactMatchPOICardLocationTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSearchNonExistingResultTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))
            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion")),
                DELAY_20000,
            )
            getInstrumentation().waitForIdleSync()
            val rvSearchPlaceSuggestion =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
            if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
                rvSearchPlaceSuggestion.adapter?.itemCount?.let {
                    if (it >= 0) {
                        onView(withId(R.id.rv_search_places_suggestion))
                            .check(matches(hasDescendant(withText(TEST_WORD_SHYAMAL_CROSS_ROAD))))

                        onView(withId(R.id.rv_search_places_suggestion))
                            .perform(
                                RecyclerViewActions.actionOnItem<ViewHolder>(
                                    hasDescendant(
                                        withText(TEST_WORD_SHYAMAL_CROSS_ROAD),
                                    ),
                                    click(),
                                ),
                            )

                        val btnDirection =
                            mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_direction)
                        if (btnDirection.visibility == View.VISIBLE) {
                            uiDevice.wait(
                                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/tv_direction_distance")),
                                DELAY_20000,
                            )
                            val tvDirectionTime =
                                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_direction_distance)
                            Assert.assertTrue(
                                TEST_FAILED_DIRECTION_TIME_NOT_VISIBLE,
                                tvDirectionTime.visibility == View.VISIBLE,
                            )
                        } else {
                            Assert.fail()
                        }
                    } else {
                        Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                    }
                }
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
