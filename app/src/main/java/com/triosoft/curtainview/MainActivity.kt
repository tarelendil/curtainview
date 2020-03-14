package com.triosoft.curtainview

import android.os.Bundle
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var velocityTracker: VelocityTracker? = null
    private var velocityMinThreshold: Int = 0
    private var velocityMaxThreshold: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val movingView = findViewById<View>(R.id.fl)
        val container = findViewById<View>(R.id.container)
        velocityMinThreshold = 3500
        velocityMaxThreshold = ViewConfiguration.get(this).scaledMaximumFlingVelocity
        movingView.post { movingView.y = container.top - movingView.height.toFloat() }
        container.setOnTouchListener(object : OnTouchListener {
            var dX = 0f
            var dY = 0f
            var isEvent = false
            var topToBottom = false
            private val offset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset)
            private var topTouchPosition = 0f
            private var bottomTouchPosition = 0f
            private var isInHighVelocityEvent = false
            private fun calculatePositions(event: MotionEvent) {
                dX = container.x - event.rawX
                dY = container.y - event.rawY
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> if (movingView.y == movingView.height * (-1).toFloat() && event.y < container.top + offset) {
                        topToBottom = true
                        topTouchPosition = event.y
                        calculatePositions(event)
                        setVelocityTracker(event)
                        isEvent = true
                    } else if (container.bottom.toFloat() == movingView.y + movingView.height && event.y > container.bottom - offset) {
                        topToBottom = false
                        bottomTouchPosition = event.y
                        calculatePositions(event)
                        setVelocityTracker(event)
                        isEvent = true
                    }
                    MotionEvent.ACTION_MOVE -> if (isEvent) {
                        val currentVelocity = getVelocity(event)!!
                        if (!isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold) {
                            isInHighVelocityEvent = true
                            movingView.animate()
                                .y(if (topToBottom) container.top.toFloat() else container.top.toFloat() - movingView.height)
                                .setDuration(200)
                                .start()
                        } else if (!isInHighVelocityEvent) {
                            movingView.animate() //                                .x(event.getRawX() + dX)
                                .y(if (topToBottom) event.y - topTouchPosition - movingView.height else -movingView.height + event.y + (container.bottom - bottomTouchPosition))
                                .setDuration(0)
                                .start()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isEvent && !isInHighVelocityEvent) {
                            if (topToBottom) {
                                if (container.bottom / 3f < event.y) {
                                    movingView.animate()
                                        .y(container.bottom - movingView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                } else {
                                    movingView.animate()
                                        .y(container.top - movingView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                }
                            } else {
                                if (container.bottom - (container.bottom / 3f) > event.y) {
                                    movingView.animate()
                                        .y(container.top - movingView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                } else {
                                    movingView.animate()
                                        .y(container.bottom - movingView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                }
                            }
                        }
                        releaseVelocityTracker()
                        isEvent = false
                        isInHighVelocityEvent = false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        releaseVelocityTracker()
                    }
                    else -> return false
                }
                return true
            }
        })
    }

    private fun setVelocityTracker(event: MotionEvent) {
        // Reset the velocity tracker back to its initial state.
        velocityTracker?.clear()
        // If necessary retrieve a new VelocityTracker object to watch the
        // velocity of a motion.
        velocityTracker = velocityTracker ?: VelocityTracker.obtain()
        // Add a user's movement to the tracker.
        velocityTracker?.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        // Return a VelocityTracker object back to be re-used by others.
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun getVelocity(event: MotionEvent) =
        velocityTracker?.run {
            val pointerId: Int = event.getPointerId(event.actionIndex)
            addMovement(event)
            // When you want to determine the velocity, call
            // computeCurrentVelocity(). Then call getXVelocity()
            // and getYVelocity() to retrieve the velocity for each pointer ID.
            computeCurrentVelocity(1000)
            // Log velocity of pixels per second
            // Best practice to use VelocityTrackerCompat where possible.
            val yVelocity = getYVelocity(pointerId)
            Timber.i("Y velocity: $yVelocity")
            yVelocity
        }
}