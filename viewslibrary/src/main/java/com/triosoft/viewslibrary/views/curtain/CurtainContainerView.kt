package com.triosoft.viewslibrary.views.curtain

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import com.triosoft.viewslibrary.R
import com.triosoft.viewslibrary.extensions.pixelToDp
import timber.log.Timber
import kotlin.math.*

/**
 * Created by ${StasK}
 */
class CurtainContainerView : LinearLayout {

    private var velocityTracker: VelocityTracker? = null
    private var velocityMinThreshold: Int = 0
    private var velocityMaxThreshold: Int = 0
    private var mTouchSlop: Int = 0
    private var offset: Int = 0

    @IdRes
    private var curtainId: Int = 0

    @IdRes
    private var actionBarId: Int = 0

    private lateinit var curtainView: View
    private var actionBarView: View? = null
    private val velocityDpPerMilliSec = 5

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
            context.obtainStyledAttributes(attrs, R.styleable.CurtainContainerView)
        curtainId =
            attributes.getResourceId(R.styleable.CurtainContainerView_ccv_curtain_view, View.NO_ID)
        if (curtainId == View.NO_ID) error("Must prove container view for the curtain view")
        actionBarId =
            attributes.getResourceId(R.styleable.CurtainContainerView_ccv_action_bar, View.NO_ID);
        initView()
        attributes.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Timber.i("onAttachedToWindow")
        curtainView = findViewById(curtainId)
        actionBarView = findViewById(actionBarId)
        curtainView.post {
            Timber.i("curtainView.post")
            curtainView.y = this@CurtainContainerView.top - curtainView.height.toFloat()
            curtainView.visibility = View.VISIBLE
        }
        setContainerOnTouchListener()
    }

    private fun initView() {
        velocityMinThreshold = 4000
        velocityMaxThreshold = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        offset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                val shouldIntercept = (curtainView.y == -curtainView.height.toFloat()
                        && ev.y < this@CurtainContainerView.top + offset)
                        || (this@CurtainContainerView.bottom.toFloat() == curtainView.y + curtainView.height
                        && ev.y > this@CurtainContainerView.bottom - offset)
                Timber.i("onInterceptTouchEvent $shouldIntercept")
                shouldIntercept
            }
            else -> {
                Timber.i("onInterceptTouchEvent else")
                false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setContainerOnTouchListener() {
        this@CurtainContainerView.setOnTouchListener(object : OnTouchListener {
            var isEvent = false
            var topToBottom = false
            private var topTouchPosition = 0f
            private var bottomTouchPosition = 0f
            private var isInHighVelocityEvent = false
            private var previousPositionY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> if (curtainView.y == -curtainView.height.toFloat() && event.y < this@CurtainContainerView.top + offset) {
                        topToBottom = true
                        topTouchPosition = event.y
                        previousPositionY = event.y
                        setVelocityTracker(event)
                        isEvent = true
                    } else if (this@CurtainContainerView.bottom.toFloat() == curtainView.y + curtainView.height && event.y > this@CurtainContainerView.bottom - offset) {
                        topToBottom = false
                        bottomTouchPosition = event.y
                        previousPositionY = event.y
                        setVelocityTracker(event)
                        isEvent = true
                    }
                    MotionEvent.ACTION_MOVE -> if (isEvent) {
                        Timber.i("ACTION_MOVE:\ncontainer.bottom ${this@CurtainContainerView.bottom} | container.top ${this@CurtainContainerView.top} | event.y ${event.y} | movingView.height ${curtainView.height} | bottomTouchPosition $bottomTouchPosition | topTouchPosition $topTouchPosition")
                        val currentVelocity = getVelocity(event)!!
                        if (!isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold) {
                            isInHighVelocityEvent = true
                            val isMovingDown = previousPositionY < event.y
                            curtainView.animate()
                                .y(if (isMovingDown) this@CurtainContainerView.top.toFloat() else this@CurtainContainerView.top.toFloat() - curtainView.height)
                                .setDuration(calcAnimationDuration(isMovingDown, event.y))
                                .start()
                            previousPositionY = event.y
                        } else if (!isInHighVelocityEvent) {
                            curtainView.animate()
                                .y(
                                    if (topToBottom) this@CurtainContainerView.top - curtainView.height + min(
                                        event.y - topTouchPosition,
                                        curtainView.height.toFloat()
                                    ) else this@CurtainContainerView.bottom - curtainView.height - (max(
                                        bottomTouchPosition - event.y,
                                        0f
                                    ))
                                )
                                .setDuration(0)
                                .start()
                            previousPositionY = event.y
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isEvent && !isInHighVelocityEvent) {
                            Timber.i("ACTION_UP")
                            if (topToBottom) {
                                if (event.y > this@CurtainContainerView.bottom * 0.4) {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.bottom - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = true,
                                                currentPositionY = event.y
                                            )
                                        )
                                        .start()
                                } else {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.top - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = false,
                                                currentPositionY = event.y
                                            )
                                        )
                                        .start()
                                }
                            } else {
                                if (event.y < this@CurtainContainerView.bottom - this@CurtainContainerView.bottom * 0.4) {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.top - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = false,
                                                currentPositionY = event.y
                                            )
                                        )
                                        .start()
                                } else {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.bottom - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = true,
                                                currentPositionY = event.y
                                            )
                                        )
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

    private fun calcAnimationDuration(isMovingDown: Boolean, currentPositionY: Float) =
        (if (isMovingDown) {
            val dpUntilBottom = resources.pixelToDp(curtainView.height - currentPositionY)
            dpUntilBottom / velocityDpPerMilliSec
        } else {
            val dpUntilTop = resources.pixelToDp(currentPositionY)
            dpUntilTop / velocityDpPerMilliSec
        } * 5).roundToLong().also { Timber.i("calcAnimationDuration: $it") }
}
