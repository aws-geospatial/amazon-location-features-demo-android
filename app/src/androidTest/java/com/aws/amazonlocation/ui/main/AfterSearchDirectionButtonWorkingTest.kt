package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
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
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_CARD
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_FAILED_SEARCH_FIELD_NOT_VISIBLE
import com.aws.amazonlocation.TEST_FAILED_SEARCH_SHEET
import com.aws.amazonlocation.TEST_WORD_RIO_TINTO
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class AfterSearchDirectionButtonWorkingTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showAfterSearchDirectionButtonWorkingTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_WORD_RIO_TINTO))

            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion")),
                DELAY_20000,
            )
            val rvSearchPlaceSuggestion =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
            if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
                rvSearchPlaceSuggestion.adapter?.itemCount?.let {
                    if (it >= 0) {
                        val clSearchSheet =
                            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_search)
                        if (clSearchSheet.visibility == View.VISIBLE) {
                            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                                BottomSheetBehavior.from(clSearchSheet)
                            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
                            uiDevice.wait(Until.hasObject(By.text(mActivityRule.activity.getString(R.string.menu_explore))), DELAY_5000)
                            val cardDirection =
                                mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_direction)
                            if (cardDirection.visibility == View.VISIBLE) {
                                val cardDirectionTest =
                                    onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
                                cardDirectionTest.perform(click())
                                uiDevice.wait(
                                    Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/edt_search_direction")),
                                    DELAY_5000,
                                )
                                val edtSearchDirection =
                                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                                Assert.assertTrue(TEST_FAILED_SEARCH_FIELD_NOT_VISIBLE, edtSearchDirection.visibility == View.VISIBLE)
                            } else {
                                Assert.fail(TEST_FAILED_DIRECTION_CARD)
                            }
                        } else {
                            Assert.fail(TEST_FAILED_SEARCH_SHEET)
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
