package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NOT_EQUAL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_RIO_TINTO
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.ui.main.explore.SearchPlacesAdapter
import com.aws.amazonlocation.ui.main.explore.SearchPlacesSuggestionAdapter
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchResultComparisonTest : BaseTestMainActivity() {
    @Test
    fun showSearchResultComparisonTest() {
        try {
            checkLocationPermission()

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_WORD_RIO_TINTO))
            val listDataSearch = arrayListOf<String>()
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
                for (i in 0 until itemCount) {
                    rvSearchPlaceSuggestion?.perform(
                        RecyclerViewActions.scrollToPosition<SearchPlacesSuggestionAdapter.SearchPlaceVH>(
                            i
                        ),
                        RecyclerViewActions.actionOnItemAtPosition<SearchPlacesSuggestionAdapter.SearchPlaceVH>(
                            i,
                            object : ViewAction {
                                override fun getConstraints(): Matcher<View> = isAssignableFrom(
                                    TextView::class.java
                                )

                                override fun getDescription(): String = "Get data from RecyclerView item"

                                override fun perform(
                                    uiController: UiController?,
                                    view: View
                                ) {
                                    val data =
                                        view.findViewById<AppCompatTextView>(R.id.tv_place_name)
                                    listDataSearch.add(data.text.toString())
                                }
                            }
                        )
                    )
                }
                pressBack()
                waitForView(
                    AllOf.allOf(
                        withText(mActivityRule.activity.getString(R.string.menu_explore)),
                        isDisplayed()
                    )
                )

                val cardDirectionTest =
                    waitForView(allOf(withId(R.id.card_direction), isDisplayed()))
                cardDirectionTest?.perform(click())
                val edtSearchDirection =
                    waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
                edtSearchDirection?.perform(click())
                onView(withId(R.id.edt_search_direction)).perform(
                    replaceText(
                        TEST_WORD_RIO_TINTO
                    )
                )

                val rvSearchPlaceDirection =
                    waitForView(
                        allOf(
                            withId(R.id.rv_search_places_suggestion_direction),
                            isDisplayed(),
                            hasMinimumChildCount(1)
                        )
                    )
                var itemCountInside = 0
                rvSearchPlaceDirection?.check { view, _ ->
                    if (view is RecyclerView) {
                        itemCountInside = view.adapter?.itemCount ?: 0
                    } else {
                        Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                    }
                }
                val listInsideDataSearch = arrayListOf<String>()
                if (itemCountInside >= 0) {
                    for (i in 0 until itemCountInside) {
                        onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                            RecyclerViewActions.scrollToPosition<SearchPlacesAdapter.SearchPlaceVH>(
                                i
                            ),
                            RecyclerViewActions.actionOnItemAtPosition<SearchPlacesAdapter.SearchPlaceVH>(
                                i,
                                object : ViewAction {
                                    override fun getConstraints(): Matcher<View> = isAssignableFrom(
                                        TextView::class.java
                                    )

                                    override fun getDescription(): String = "Get data from RecyclerView item"

                                    override fun perform(
                                        uiController: UiController?,
                                        view: View
                                    ) {
                                        val data =
                                            view.findViewById<AppCompatTextView>(
                                                R.id.tv_place_name
                                            )
                                        listInsideDataSearch.add(data.text.toString())
                                    }
                                }
                            )
                        )
                    }
                }
                Assert.assertTrue(
                    TEST_FAILED_NOT_EQUAL,
                    listDataSearch == listInsideDataSearch
                )
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
