package com.aws.amazonlocation.ui.main.bug

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_GUOCO_MIDTOWN_SQUARE
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class AfterSearchDirectionErrorExistsTest : BaseTestMainActivity() {
    @Test
    fun showAfterSearchDirectionErrorExistsTest() {
        try {
            checkLocationPermission()

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(
                replaceText(
                    TEST_WORD_GUOCO_MIDTOWN_SQUARE
                )
            )
            BuildConfig.APPLICATION_ID
            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
            val rvSearchPlaceSuggestion =
                waitForView(
                    allOf(
                        withId(R.id.rv_search_places_suggestion),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )
            var itemCount = 0
            rvSearchPlaceSuggestion?.check { view, _ ->
                if (view is RecyclerView) {
                    itemCount = view.adapter?.itemCount ?: 0
                } else {
                    Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                }
            }
            if (itemCount >= 0) {
                onView(withId(R.id.rv_search_places_suggestion))?.perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )
                waitForView(
                    allOf(
                        withId(R.id.tv_direction_time),
                        isDisplayed()
                    )
                )
                waitForView(
                    allOf(
                        withId(R.id.btn_direction),
                        isDisplayed()
                    )
                )
                waitForView(
                    allOf(
                        withId(R.id.tv_direction_error_2),
                        isDisplayed()
                    )
                )
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
