package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DUE_TO_WORD_MISMATCHED
import com.aws.amazonlocation.TEST_WORD_ARRIVE
import com.aws.amazonlocation.TEST_WORD_AUBURN_SYDNEY
import com.aws.amazonlocation.TEST_WORD_LEAVE
import com.aws.amazonlocation.TEST_WORD_MANLY_BEACH_SYDNEY
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ChangeMultipleDepartOptionsTest : BaseTestMainActivity() {

    @Test
    fun changeMultipleDepartOptionsTest() {
        try {
            checkLocationPermission()

            val cardDirection =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirection.perform(click())

            val edtSearchDirection = waitForView(
                CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed())
            )
            edtSearchDirection?.perform(replaceText(TEST_WORD_AUBURN_SYDNEY))

            var rvSearchPlaces = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
            rvSearchPlaces?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            val edtSearchDest = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed()
                )
            )
            edtSearchDest?.perform(
                click(),
                replaceText(TEST_WORD_MANLY_BEACH_SYDNEY)
            )

            rvSearchPlaces = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
            rvSearchPlaces?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            val cardDepartOptions = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_depart_options),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )
            cardDepartOptions?.perform(click())

            val clArriveBy = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_arrive_by),
                    isDisplayed()
                )
            )
            clArriveBy?.perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_drive_loader),
                    isDisplayed()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            var tvDepartOptions = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.tv_depart_options),
                    isDisplayed()
                )
            )
            tvDepartOptions?.check { view, _ ->
                if (view is AppCompatTextView) {
                    Assert.assertTrue(
                        TEST_FAILED_DUE_TO_WORD_MISMATCHED,
                        view.text.contains(TEST_WORD_ARRIVE, true)
                    )
                } else {
                    Assert.fail(TEST_FAILED)
                }
            }

            val clLeaveAt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_leave_at),
                    isDisplayed()
                )
            )
            clLeaveAt?.perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_drive_loader),
                    isDisplayed()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            tvDepartOptions = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.tv_depart_options),
                    isDisplayed()
                )
            )

            tvDepartOptions?.check { view, _ ->
                if (view is AppCompatTextView) {
                    Assert.assertTrue(
                        TEST_FAILED_DUE_TO_WORD_MISMATCHED,
                        view.text.contains(TEST_WORD_LEAVE, true)
                    )
                } else {
                    Assert.fail(TEST_FAILED)
                }
            }
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
