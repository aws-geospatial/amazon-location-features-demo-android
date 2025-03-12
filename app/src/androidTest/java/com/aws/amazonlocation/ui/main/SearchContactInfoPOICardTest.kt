package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_DOMINO_PIZZA
import com.aws.amazonlocation.TEST_WORD_DOMINO_PIZZA_VEJALPUR
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
class SearchContactInfoPOICardTest : BaseTestMainActivity() {
    @Test
    fun searchContactInfoPOICardTest() {
        try {
            checkLocationPermission()

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(
                replaceText(
                    TEST_WORD_DOMINO_PIZZA_VEJALPUR
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
                onView(withId(R.id.rv_search_places_suggestion))
                    .check(matches(hasDescendant(withText(TEST_WORD_DOMINO_PIZZA))))
                onView(withId(R.id.rv_search_places_suggestion))
                    .perform(
                        RecyclerViewActions.actionOnItem<ViewHolder>(
                            hasDescendant(
                                withText(TEST_WORD_DOMINO_PIZZA)
                            ),
                            click()
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
                        withId(R.id.tv_place_link),
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
