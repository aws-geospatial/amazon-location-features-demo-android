package com.aws.amazonlocation.actions

import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import com.aws.amazonlocation.NESTED_SCROLL_ERROR
import org.hamcrest.Matcher
import org.hamcrest.Matchers

fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf(
                ViewMatchers.isDescendantOfA(
                    ViewMatchers.isAssignableFrom(NestedScrollView::class.java)
                ),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        }

        override fun getDescription(): String {
            return NESTED_SCROLL_ERROR
        }

        override fun perform(uiController: UiController?, view: View?) {
            try {
                val nestedScrollView = view?.let {
                    findFirstParentLayoutOfClass(
                        it,
                        NestedScrollView::class.java
                    )
                } as NestedScrollView?
                if (nestedScrollView != null) {
                    view?.let { nestedScrollView.scrollTo(0, it.top) }
                } else {
                    throw java.lang.Exception("Unable to find NestedScrollView parent.")
                }
            } catch (e: java.lang.Exception) {
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(e)
                    .build()
            }
            uiController?.loopMainThreadUntilIdle()
        }
    }
}

private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View? {
    var parent: ViewParent = FrameLayout(view.context)
    var incrementView: ViewParent? = null
    var i = 0
    while (parent.javaClass != parentClass) {
        parent = if (i == 0) {
            findParent(view)
        } else {
            if (incrementView != null) {
                findParent(incrementView)
            } else {
                return null
            }
        }
        incrementView = parent
        i++
    }
    return parent as View
}

private fun findParent(view: View): ViewParent {
    return view.parent
}

private fun findParent(view: ViewParent): ViewParent {
    return view.parent
}

@Suppress("DEPRECATION")
fun clickXYPercent(x: Float, y: Float): ViewAction {
    return GeneralClickAction(
        Tap.SINGLE,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val width = view.width
            val height = view.height

            val xPoint = (width / 100) * x
            val yPoint = (height / 100) * y

            floatArrayOf(xPoint, yPoint)
        },
        Press.FINGER
    )
}

fun clickOnViewChild(viewId: Int) = object : ViewAction {
    override fun getConstraints() = null

    override fun getDescription() = "Click on a child view with specified id."

    override fun perform(uiController: UiController, view: View) = ViewActions.click()
        .perform(uiController, view.findViewById(viewId))
}
