package com.triosoft.android.viewslibrary.views.curtain

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AlphaAnimation
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.triosoft.android.viewslibrary.R
import com.triosoft.android.viewslibrary.extensions.pixelToDp
import com.triosoft.android.viewslibrary.extensions.setAnimationEndListener
import com.triosoft.android.viewslibrary.extensions.setOneTimeGlobalLayoutObserver
import timber.log.Timber
import kotlin.math.*


/**
 * Created by ${StasK}
 */
class CurtainContainerView : ConstraintLayout {

    private var velocityTracker: VelocityTracker? = null
    private var velocityMinThreshold: Int = 0
    private var velocityMaxThreshold: Int = 0
    private var touchSlop: Int = 0
    private var topOffset: Int = 0
    private var bottomOffset: Int = 0

    @IdRes
    private var curtainId: Int = 0

    @IdRes
    private var actionBarId: Int = 0

    private lateinit var curtainView: View
    private var actionBarView: View? = null
    private val velocityDpPerMilliSec = 5

    private var isEvent = false
    private var isTopToBottom = false
    private var topTouchPosition = 0f
    private var bottomTouchPosition = 0f
    private var isInHighVelocityEvent = false
    private var previousPositionY = 0f
    private var wasMovingDown = true
    private var alphaAnimationDurationMillis = 700L
    private var shouldCheckSlop = false
    private var interceptedEventDownYPosition = 0f

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
        if (curtainId == View.NO_ID) error("Must provide container view for the curtain view")
        actionBarId =
            attributes.getResourceId(R.styleable.CurtainContainerView_ccv_action_bar, View.NO_ID)
        velocityMinThreshold = attributes.getInteger(
            R.styleable.CurtainContainerView_ccv_velocity_minimum_threshold,
            3000
        )
        alphaAnimationDurationMillis = attributes.getInteger(
            R.styleable.CurtainContainerView_ccv_alpha_animation_duration_millis,
            700
        ).toLong()

        initView()
        attributes.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        curtainView = findViewById(curtainId)
        actionBarView = findViewById(actionBarId)
        if (actionBarView != null) {
            topOffset = 0
            actionBarView!!.setOneTimeGlobalLayoutObserver {
                Timber.i("actionBarView?.viewTreeObserver")
                topOffset = actionBarView!!.height
            }
        } else topOffset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset)

        curtainView.setOneTimeGlobalLayoutObserver {
            Timber.i("curtainView.viewTreeObserver")
            curtainView.y = this@CurtainContainerView.top - curtainView.height.toFloat()
            curtainView.visibility = View.VISIBLE
        }
        this.setOneTimeGlobalLayoutObserver{
            bottomOffset = this.height
        }
        setContainerOnTouchListener()
    }

    private fun initView() {
        velocityMaxThreshold = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (curtainView.y == -curtainView.height.toFloat()
                    && ev.y < this@CurtainContainerView.top + topOffset
                ) {
                    shouldCheckSlop = true
                    setEventProperties(true, ev)
                } else if (this@CurtainContainerView.bottom.toFloat() == curtainView.y + curtainView.height
                    && ev.y > this@CurtainContainerView.bottom - bottomOffset
                ) {
                    shouldCheckSlop = true
                    setEventProperties(false, ev)
                } else shouldCheckSlop = false
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (shouldCheckSlop && (ev.y - interceptedEventDownYPosition).absoluteValue > touchSlop) {
                    isEvent = true
                    true
                } else false
            }

            else -> false.also {
                shouldCheckSlop = false
                Timber.i("onInterceptTouchEvent else")
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setContainerOnTouchListener() {
        this@CurtainContainerView.setOnTouchListener { v, event ->
            var eventConsumed = false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> if (curtainView.y == -curtainView.height.toFloat() && (event.y < this@CurtainContainerView.top + topOffset)) {
                    animateActionBarViewAlpha(toAlpha = true)
                    setEventProperties(true, event)
                    isEvent = true
                    eventConsumed = true
                } else if (this@CurtainContainerView.bottom.toFloat() == curtainView.y + curtainView.height && event.y > this@CurtainContainerView.bottom - bottomOffset) {
                    setEventProperties(false, event)
                    isEvent = true
                    eventConsumed = true
                }
                MotionEvent.ACTION_MOVE -> if (isEvent) {
                    Timber.i("ACTION_MOVE:\ncontainer.bottom ${this@CurtainContainerView.bottom} | container.top ${this@CurtainContainerView.top} | event.y ${event.y} | movingView.height ${curtainView.height} | bottomTouchPosition $bottomTouchPosition | topTouchPosition $topTouchPosition")
                    wasMovingDown = previousPositionY < event.y
                    val currentVelocity = getVelocity(event) ?: 0f
                    if (!isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold) {
                        isInHighVelocityEvent = true
                    } else if (isInHighVelocityEvent && currentVelocity.roundToInt().absoluteValue !in velocityMinThreshold..velocityMaxThreshold) {
                        isInHighVelocityEvent = false
                    }
                    curtainView.animate()
                        .y(
                            if (isTopToBottom) this@CurtainContainerView.top - curtainView.height + min(
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
                    eventConsumed = true
                }
                MotionEvent.ACTION_UP -> {
                    if (isEvent) {
                        if (isInHighVelocityEvent) {
                            curtainView.animate()
                                .y(if (wasMovingDown) this@CurtainContainerView.top.toFloat() else this@CurtainContainerView.top.toFloat() - curtainView.height)
                                .setDuration(
                                    calcAnimationDuration(
                                        wasMovingDown,
                                        curtainView.y + curtainView.height
                                    )
                                )
                                .setAnimationEndListener {
                                    if (!wasMovingDown) animateActionBarViewAlpha(
                                        toAlpha = false
                                    )
                                }.start()
                        } else {
                            if (isTopToBottom) {
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
                                        .setAnimationEndListener { animateActionBarViewAlpha(toAlpha = false) }
                                        .start()
                                }
                            } else {
                                if (curtainView.y + curtainView.height < this@CurtainContainerView.bottom * 0.6) {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.top - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = false,
                                                currentPositionY = curtainView.y + curtainView.height
                                            )
                                        )
                                        .setAnimationEndListener { animateActionBarViewAlpha(toAlpha = false) }
                                        .start()
                                } else {
                                    curtainView.animate()
                                        .y(this@CurtainContainerView.bottom - curtainView.height.toFloat())
                                        .setDuration(
                                            calcAnimationDuration(
                                                isMovingDown = true,
                                                currentPositionY = curtainView.y + curtainView.height
                                            )
                                        ).start()
                                }
                            }
                        }
                        eventConsumed = true
                        releaseVelocityTracker()
                        isEvent = false
                        isInHighVelocityEvent = false
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    isEvent = false
                    isInHighVelocityEvent = false
                    releaseVelocityTracker()
                    eventConsumed = true
                }
            }
            eventConsumed
        }
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
            val dpUntilBottom = max(
                0f,
                resources.pixelToDp(curtainView.height - currentPositionY)
            )
            dpUntilBottom / velocityDpPerMilliSec
        } else {
            val dpUntilTop = resources.pixelToDp(max(0f, currentPositionY))
            dpUntilTop / velocityDpPerMilliSec
        } * 5).roundToLong().also { Timber.i("calcAnimationDuration: $it") }


    private fun animateActionBarViewAlpha(toAlpha: Boolean) {
        actionBarView?.let { view ->
            AlphaAnimation(if (toAlpha) 1f else 0.1f, if (toAlpha) 0.1f else 1f).let { anim ->
                anim.duration = alphaAnimationDurationMillis
                anim.fillAfter = true
                view.startAnimation(anim)
            }
        }
    }

    private fun setEventProperties(isTopToBottom: Boolean, event: MotionEvent) {
        this.isTopToBottom = isTopToBottom
        previousPositionY = event.y
        bottomTouchPosition = event.y
        setVelocityTracker(event)
    }
}
