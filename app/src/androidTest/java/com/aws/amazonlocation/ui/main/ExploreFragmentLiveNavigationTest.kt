package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_LIST
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
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
class ExploreFragmentLiveNavigationTest : BaseTestMainActivity() {
    @Test
    fun showLiveNavigationTest() {
        try {
            checkLocationPermission()

            val edtSearch = waitForView(allOf(withId(R.id.edt_search_places), isDisplayed()))
            edtSearch?.perform(click(), replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))

            val rvRecyclerView =
                waitForView(
                    allOf(
                        withId(R.id.rv_search_places_suggestion),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )
            rvRecyclerView?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            // txtTime
            waitForView(
                allOf(
                    withId(R.id.tv_direction_distance),
                    isDisplayed()
                )
            )

            val btnDirection =
                waitForView(
                    allOf(
                        withId(R.id.btn_direction),
                        isDisplayed()
                    )
                )
            btnDirection?.perform(click())

            val btnCarGo =
                waitForView(
                    allOf(
                        withId(R.id.card_drive_go),
                        hasDescendant(
                            withText(GO)
                        ),
                        isDisplayed()
                    )
                )
            btnCarGo?.perform(click())

            Espresso.closeSoftKeyboard()

            // navListView
            waitForView(
                allOf(
                    withId(R.id.rv_navigation_list),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED_LIST ${e.message}")
        }
    }
}
