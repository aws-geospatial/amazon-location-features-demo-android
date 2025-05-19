package com.aws.amazonlocation.ui.main

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchCollapseTest : BaseTestMainActivity() {
    @Test
    fun showSearchCollapseTest() {
        checkLocationPermission()
        waitForView(
            allOf(
                withId(R.id.edt_search_places),
                isDisplayed()
            )
        )
        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        edtSearch.perform(ViewActions.closeSoftKeyboard())

        val clSearchSheet =
            waitForView(
                allOf(
                    withId(R.id.bottom_sheet_search),
                    isDisplayed()
                )
            )
        clSearchSheet?.check { view, _ ->
            if (view is ConstraintLayout) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(view)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_navigate)),
                isDisplayed()
            )
        )
        waitForView(
            allOf(
                withId(R.id.card_navigation),
                isDisplayed()
            )
        )
    }
}
