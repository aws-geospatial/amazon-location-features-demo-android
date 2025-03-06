package com.aws.amazonlocation.ui.main

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_HEIGHT_NOT_GREATER
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchExistsTest : BaseTestMainActivity() {
    @Test
    fun showSearchExistsTest() {
        try {
            checkLocationPermission()
            val edtSearchPlaces =
                waitForView(
                    allOf(
                        withId(R.id.edt_search_places),
                        isDisplayed()
                    )
                )
            val point = IntArray(2)
            edtSearchPlaces?.check { view, _ ->
                if (view is TextInputEditText) {
                    view.getLocationOnScreen(point)

                    val screenHeight = mActivityRule.activity.window.decorView.height / 2
                    Assert.assertTrue(
                        TEST_FAILED_HEIGHT_NOT_GREATER,
                        point[1] + view.height > screenHeight
                    )
                }
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
