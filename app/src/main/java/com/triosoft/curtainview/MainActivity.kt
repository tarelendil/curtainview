//package com.triosoft.curtainview
//
//import android.os.Bundle
//import android.view.MotionEvent
//import android.view.VelocityTracker
//import android.view.View
//import android.view.View.OnTouchListener
//import android.view.ViewConfiguration
//import androidx.appcompat.app.AppCompatActivity
//import timber.log.Timber
//import kotlin.math.absoluteValue
//import kotlin.math.roundToInt
//
//class MainActivity : AppCompatActivity() {
//    private var velocityTracker: VelocityTracker? = null
//    private var velocityMinThreshold: Int = 0
//    private var velocityMaxThreshold: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        val movingView = findViewById<View>(R.id.fl)
//        val container = findViewById<View>(R.id.container)
//        velocityMinThreshold = 3500
//        velocityMaxThreshold = ViewConfiguration.get(this).scaledMaximumFlingVelocity
//        movingView.post { movingView.y = container.top - movingView.height.toFloat() }
//        container.setOnTouchListener(object : OnTouchListener {
//            var isEvent = false
//            var topToBottom = false
//            private val offset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset)
//            private var topTouchPosition = 0f
//            private var bottomTouchPosition = 0f
//            private var isInHighVelocityEvent = false
//            private var previousPositionY = 0f
//
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.actionMasked) {
//                    MotionEvent.ACTION_DOWN -> if (movingView.y == -movingView.height.toFloat() && event.y < container.top + offset) {
//                        topToBottom = true
//                        topTouchPosition = event.y
//                        previousPositionY = event.y
//                        setVelocityTracker(event)
//                        isEvent = true
//                    } else if (container.bottom.toFloat() == movingView.y + movingView.height && event.y > container.bottom - offset) {
//                        topToBottom = false
//                        bottomTouchPosition = event.y
//                        previousPositionY = event.y
//                        setVelocityTracker(event)
//                        isEvent = true
//                    }
//                    MotionEvent.ACTION_MOVE -> if (isEvent) {
//                        Timber.i("ACTION_MOVE:\ncontainer.bottom ${container.bottom} | container.top ${container.top} | event.y ${event.y} | movingView.height ${movingView.height} | bottomTouchPosition $bottomTouchPosition | topTouchPosition $topTouchPosition")
////                        var directionChanged = false
////                        if (topToBottom && previousPositionY > event.y) {
////                            topToBottom = false
////                            topTouchPosition = 0f
////                            directionChanged = true
////                        } else if (!topToBottom && previousPositionY < event.y) {
////                            topToBottom = true
////                            bottomTouchPosition = 0f
////                            directionChanged = true
////                        }
//                            val currentVelocity = getVelocity(event)!!
//                            if (!isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold) {
//                                isInHighVelocityEvent = true
//                                movingView.animate()
//                                    .y(if (topToBottom) container.top.toFloat() else container.top.toFloat() - movingView.height)
//                                    .setDuration(200)
//                                    .start()
//                                previousPositionY = event.y
//                            } else if (!isInHighVelocityEvent) {
//                                movingView.animate()
//                                    .y(if (topToBottom) container.top - movingView.height + (event.y - topTouchPosition) else container.bottom - movingView.height - (bottomTouchPosition - event.y))
//                                    .setDuration(0)
//                                    .start()
//                                previousPositionY = event.y
//                            }
//                        }
//                    MotionEvent.ACTION_UP -> {
//                        if (isEvent && !isInHighVelocityEvent) {
//                            if (topToBottom) {
//                                if (container.bottom / 3f < event.y) {
//                                    movingView.animate()
//                                        .y(container.bottom - movingView.height.toFloat())
//                                        .setDuration(200)
//                                        .start()
//                                } else {
//                                    movingView.animate()
//                                        .y(container.top - movingView.height.toFloat())
//                                        .setDuration(200)
//                                        .start()
//                                }
//                            } else {
//                                if (container.bottom - (container.bottom / 3f) > event.y) {
//                                    movingView.animate()
//                                        .y(container.top - movingView.height.toFloat())
//                                        .setDuration(200)
//                                        .start()
//                                } else {
//                                    movingView.animate()
//                                        .y(container.bottom - movingView.height.toFloat())
//                                        .setDuration(200)
//                                        .start()
//                                }
//                            }
//                        }
//                        releaseVelocityTracker()
//                        isEvent = false
//                        isInHighVelocityEvent = false
//                    }
//                    MotionEvent.ACTION_CANCEL -> {
//                        releaseVelocityTracker()
//                    }
//                    else -> return false
//                }
//                return true
//            }
//        })
//    }
//
//    private fun setVelocityTracker(event: MotionEvent) {
//        // Reset the velocity tracker back to its initial state.
//        velocityTracker?.clear()
//        // If necessary retrieve a new VelocityTracker object to watch the
//        // velocity of a motion.
//        velocityTracker = velocityTracker ?: VelocityTracker.obtain()
//        // Add a user's movement to the tracker.
//        velocityTracker?.addMovement(event)
//    }
//
//    private fun releaseVelocityTracker() {
//        // Return a VelocityTracker object back to be re-used by others.
//        velocityTracker?.recycle()
//        velocityTracker = null
//    }
//
//    private fun getVelocity(event: MotionEvent) =
//        velocityTracker?.run {
//            val pointerId: Int = event.getPointerId(event.actionIndex)
//            addMovement(event)
//            // When you want to determine the velocity, call
//            // computeCurrentVelocity(). Then call getXVelocity()
//            // and getYVelocity() to retrieve the velocity for each pointer ID.
//            computeCurrentVelocity(1000)
//            // Log velocity of pixels per second
//            // Best practice to use VelocityTrackerCompat where possible.
//            val yVelocity = getYVelocity(pointerId)
//            Timber.i("Y velocity: $yVelocity")
//            yVelocity
//        }
//}