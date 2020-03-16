package com.triosoft.viewslibrary.views.curtain

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import androidx.annotation.IdRes
import com.triosoft.viewslibrary.R
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Created by ${StasK}
 */
class CurtainView : LinearLayout {
    private var velocityTracker: VelocityTracker? = null
    private var velocityMinThreshold: Int = 0
    private var velocityMaxThreshold: Int = 0

    @IdRes
    private var containerId: Int = 0

    @IdRes
    private var actionBarId: Int = 0

    private lateinit var containerView: View
    private var actionBarView: View? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        getAttrs(attrs!!)

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getAttrs(attrs!!)

    }

    private fun getAttrs(attrs: AttributeSet) {
        val attributes: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CurtainView)
        containerId = attributes.getResourceId(R.styleable.CurtainView_cv_container, View.NO_ID)
        if (containerId == View.NO_ID) error("Must prove container view for the curtain view")
        actionBarId = attributes.getResourceId(R.styleable.CurtainView_cv_action_bar, View.NO_ID);
        initView()
        attributes.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        containerView = parent as View
        actionBarView = (parent as View).findViewById(actionBarId)
        setContainerOnTouchListener()
    }

    private fun initView() {
        velocityMinThreshold = 3500
        velocityMaxThreshold = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        this.post { this.y = containerView.top - this.height.toFloat() }
    }

    private fun setContainerOnTouchListener() {
        containerView.setOnTouchListener(object : OnTouchListener {
            var isEvent = false
            var topToBottom = false
            private val offset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset)
            private var topTouchPosition = 0f
            private var bottomTouchPosition = 0f
            private var isInHighVelocityEvent = false
            private var previousPositionY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> if (this@CurtainView.y == -this@CurtainView.height.toFloat() && event.y < containerView.top + offset) {
                        topToBottom = true
                        topTouchPosition = event.y
                        previousPositionY = event.y
                        setVelocityTracker(event)
                        isEvent = true
                    } else if (containerView.bottom.toFloat() == this@CurtainView.y + this@CurtainView.height && event.y > containerView.bottom - offset) {
                        topToBottom = false
                        bottomTouchPosition = event.y
                        previousPositionY = event.y
                        setVelocityTracker(event)
                        isEvent = true
                    }
                    MotionEvent.ACTION_MOVE -> if (isEvent) {
                        Timber.i("ACTION_MOVE:\ncontainer.bottom ${containerView.bottom} | container.top ${containerView.top} | event.y ${event.y} | movingView.height ${this@CurtainView.height} | bottomTouchPosition $bottomTouchPosition | topTouchPosition $topTouchPosition")
//                        var directionChanged = false
//                        if (topToBottom && previousPositionY > event.y) {
//                            topToBottom = false
//                            topTouchPosition = 0f
//                            directionChanged = true
//                        } else if (!topToBottom && previousPositionY < event.y) {
//                            topToBottom = true
//                            bottomTouchPosition = 0f
//                            directionChanged = true
//                        }
                        val currentVelocity = getVelocity(event)!!
                        if (!isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold) {
                            isInHighVelocityEvent = true
                            this@CurtainView.animate()
                                .y(if (topToBottom) containerView.top.toFloat() else containerView.top.toFloat() - this@CurtainView.height)
                                .setDuration(200)
                                .start()
                            previousPositionY = event.y
                        } else if (!isInHighVelocityEvent) {
                            this@CurtainView.animate()
                                .y(if (topToBottom) containerView.top - this@CurtainView.height + (event.y - topTouchPosition) else containerView.bottom - this@CurtainView.height - (bottomTouchPosition - event.y))
                                .setDuration(0)
                                .start()
                            previousPositionY = event.y
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isEvent && !isInHighVelocityEvent) {
                            if (topToBottom) {
                                if (containerView.bottom / 3f < event.y) {
                                    this@CurtainView.animate()
                                        .y(containerView.bottom - this@CurtainView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                } else {
                                    this@CurtainView.animate()
                                        .y(containerView.top - this@CurtainView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                }
                            } else {
                                if (containerView.bottom - (containerView.bottom / 3f) > event.y) {
                                    this@CurtainView.animate()
                                        .y(containerView.top - this@CurtainView.height.toFloat())
                                        .setDuration(200)
                                        .start()
                                } else {
                                    this@CurtainView.animate()
                                        .y(containerView.bottom - this@CurtainView.height.toFloat())
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