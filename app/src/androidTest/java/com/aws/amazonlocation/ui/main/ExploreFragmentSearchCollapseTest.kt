package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_NAVIGATION_CARD_NOT_VISIBLE
import com.aws.amazonlocation.TEST_FAILED_SEARCH_SHEET
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchCollapseTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSearchCollapseTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        uiDevice.wait(
            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/edt_search_places")),
            DELAY_5000
        )
        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        edtSearch.perform(ViewActions.closeSoftKeyboard())
        val clSearchSheet =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_search)
        if (clSearchSheet.visibility == View.VISIBLE) {
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clSearchSheet)
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
            uiDevice.wait(Until.hasObject(By.text(mActivityRule.activity.getString(R.string.menu_explore))), DELAY_5000)
            val cardNavigation =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_navigation)
            Assert.assertTrue(TEST_FAILED_NAVIGATION_CARD_NOT_VISIBLE, cardNavigation.visibility == View.VISIBLE)
        } else {
            Assert.fail(TEST_FAILED_SEARCH_SHEET)
        }
    }
}
