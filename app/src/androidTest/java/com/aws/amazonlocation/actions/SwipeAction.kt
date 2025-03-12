package com.aws.amazonlocation.actions

import android.graphics.Point
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.InjectEventSecurityException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

fun swipeLeft(): ViewAction? {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isEnabled()
        }

        override fun getDescription(): String {
            return "Swipe left"
        }

        override fun perform(uiController: UiController, view: View) {
            val viewPosition = getLocationOnScreen(view)
            val middlePosition: Point = getCenterPoint(view)
            val startDelta = (view.width * 0.5).toInt()
            val endDelta = (view.width * 0.05).toInt()
            val startPoint1 = Point(viewPosition.x + startDelta, middlePosition.y)
            val endPoint1 = Point(viewPosition.x + endDelta, middlePosition.y)

            performSwipe(uiController, startPoint1, endPoint1)
        }
    }
}

fun swipeRight(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isEnabled()
        }

        override fun getDescription(): String {
            return "Swipe right"
        }

        override fun perform(uiController: UiController, view: View) {
            val viewPosition = getLocationOnScreen(view)
            val middlePosition: Point = getCenterPoint(view)
            val startDelta = (view.width * 0.5).toInt()
            val endDelta = (view.width * 0.95).toInt()
            val startPoint1 = Point(viewPosition.x + startDelta, middlePosition.y)
            val endPoint1 = Point(viewPosition.x + endDelta, middlePosition.y)

            performSwipe(uiController, startPoint1, endPoint1)
        }
    }
}

private fun getCenterPoint(view: View): Point {
    val locationOnScreen = IntArray(2)
    view.getLocationOnScreen(locationOnScreen)
    val viewHeight = view.height * view.scaleY
    val viewWidth = view.width * view.scaleX
    return Point(
        (locationOnScreen[0] + viewWidth / 2).toInt(),
        (locationOnScreen[1] + viewHeight / 2).toInt()
    )
}

private fun getLocationOnScreen(view: View): Point {
    val locationOnScreen = IntArray(2)
    view.getLocationOnScreen(locationOnScreen)
    return Point(locationOnScreen[0], locationOnScreen[1])
}

private fun performSwipe(
    uiController: UiController,
    startPoint1: Point,
    endPoint1: Point
) {
    val duration = 50
    val eventMinInterval: Long = 10
    val startTime = SystemClock.uptimeMillis()
    var eventTime = startTime
    var event: MotionEvent
    var eventX1: Long
    var eventY1: Long
    eventX1 = startPoint1.x.toLong()
    eventY1 = startPoint1.y.toLong()

    // Specify the property for the two touch points
    val properties = arrayOfNulls<MotionEvent.PointerProperties>(1)
    val pp1 = MotionEvent.PointerProperties()
    pp1.id = 0
    pp1.toolType = MotionEvent.TOOL_TYPE_FINGER
    properties[0] = pp1

    // Specify the coordinations of the two touch points
    // NOTE: you MUST set the pressure and size value, or it doesn't work
    val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(1)
    val pc1 = MotionEvent.PointerCoords()
    pc1.x = eventX1.toFloat()
    pc1.y = eventY1.toFloat()
    pc1.pressure = 1f
    pc1.size = 1f
    pointerCoords[0] = pc1

    /*
     * Events sequence of zoom gesture:
     *
     * 1. Send ACTION_DOWN event of one start point
     * 2. Repeat step 3 with updated middle points (x,y), until reach the end points
     * 4. Send ACTION_UP of one end point
     */try {
        // Step 1
        event = MotionEvent.obtain(
            startTime, eventTime,
            MotionEvent.ACTION_DOWN, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        injectMotionEventToUiController(uiController, event)

        // Step 2
        val moveEventNumber = duration / eventMinInterval
        val stepX1: Long
        val stepY1: Long
        stepX1 = (endPoint1.x - startPoint1.x) / moveEventNumber
        stepY1 = (endPoint1.y - startPoint1.y) / moveEventNumber
        for (i in 0 until moveEventNumber) {
            // Update the move events
            eventTime += eventMinInterval
            eventX1 += stepX1
            eventY1 += stepY1
            pc1.x = eventX1.toFloat()
            pc1.y = eventY1.toFloat()
            pointerCoords[0] = pc1
            event = MotionEvent.obtain(
                startTime, eventTime,
                MotionEvent.ACTION_MOVE, 1, properties,
                pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
            )
            injectMotionEventToUiController(uiController, event)
        }

        // Step 3
        eventTime += eventMinInterval
        event = MotionEvent.obtain(
            startTime, eventTime,
            MotionEvent.ACTION_UP, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        injectMotionEventToUiController(uiController, event)
    } catch (e: InjectEventSecurityException) {
        throw RuntimeException("Could not perform swipe", e)
    }
}

/**
 * Safely call uiController.injectMotionEvent(event): Detect any error and "convert" it to an
 * IllegalStateException
 */
@Throws(InjectEventSecurityException::class)
private fun injectMotionEventToUiController(uiController: UiController, event: MotionEvent) {
    val injectEventSucceeded = uiController.injectMotionEvent(event)
    check(injectEventSucceeded) { "Error performing event $event" }
}
