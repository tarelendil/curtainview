package com.stas.android.curtainview.views.curtain

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.*
import android.view.animation.AlphaAnimation
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.stas.android.curtainview.R
import com.stas.android.curtainview.extensions.pixelToDp
import com.stas.android.curtainview.extensions.setAnimationEndListener
import com.stas.android.curtainview.extensions.setGlobalLayoutObserver
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
    private var waitForActionBarVisibility = false
    private var lastHighVelocityValue = 0f
    private var curtainViewHeight: Int = -1
    private var actionBarHeight: Int = -1
    private var containerHeight: Int = -1
    private var shouldAlphaAnimateActionBar = false
    private var curtainEnabled = true

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
        waitForActionBarVisibility = attributes.getBoolean(
            R.styleable.CurtainContainerView_ccv_wait_for_action_bar_visibility,
            false
        )
        shouldAlphaAnimateActionBar = attributes.getBoolean(
            R.styleable.CurtainContainerView_ccv_should_alpha_animate_action_bar,
            false
        )
        initView()
        attributes.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        curtainView = findViewById(curtainId)
        actionBarView = findViewById(actionBarId)
        if (actionBarView != null) {
            topOffset = 0
            actionBarView!!.setGlobalLayoutObserver {
                if (actionBarView!!.height != actionBarHeight) {
                    Timber.i("actionBarView viewTreeObserver")
                    Timber.i("old height $actionBarHeight new height:${actionBarView!!.height}")
                    topOffset = actionBarView!!.height
                    actionBarHeight = actionBarView!!.height
                    Timber.i("topOffset $topOffset")
                }
                false
            }
        } else topOffset = resources.getDimensionPixelSize(R.dimen.swipeStartPositionOffset).also {
            Timber.i("topOffset = resources.getDimensionPixelSize $it")
        }
        curtainView.setGlobalLayoutObserver {
            if (curtainView.height != curtainViewHeight) {
                Timber.i("curtainView viewTreeObserver")
                Timber.i("old height $curtainViewHeight new height:${curtainView.height}")
                curtainViewHeight = curtainView.height
                curtainView.y = this@CurtainContainerView.top - curtainView.height.toFloat()
                Timber.i("curtainView.y ${curtainView.y}")
                curtainView.visibility = View.VISIBLE
            }
            false
        }
        this.setGlobalLayoutObserver {
            if (this.height != containerHeight) {
                Timber.i("container viewTreeObserver")
                Timber.i("old height $containerHeight new height:${this.height}")
                containerHeight = this.height
                bottomOffset = this.height
                Timber.i("bottomOffset $bottomOffset")
            }
            false
        }

        setContainerOnTouchListener()
    }

    private fun initView() {
        if (velocityMinThreshold == 0) {
            velocityMinThreshold = ViewConfiguration.get(context).scaledMinimumFlingVelocity * 10
        }
        velocityMaxThreshold = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop.also {
            Timber.i("slope$it")
        }
    }

    /**
     * If we start the touch over a child view, for example a button, we would use the touch slop
     * to check if the user just wanted to touch the button or move the curtain view
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        if (!curtainEnabled) false
        else when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    isTouchAtTopPosition(event) -> {
                        shouldCheckSlop = true
                        interceptedEventDownYPosition = event.y
                        setEventProperties(true, event)
                    }
                    isTouchAtBottomPosition(event) -> {
                        shouldCheckSlop = true
                        interceptedEventDownYPosition = event.y
                        setEventProperties(false, event)
                    }
                    else -> shouldCheckSlop = false
                }
                false
            }
            MotionEvent.ACTION_MOVE -> {
                if (shouldCheckSlop && (event.y - interceptedEventDownYPosition).absoluteValue > touchSlop) {
                    isEvent = true
                    true
                } else false
            }

            else -> false.also {
                shouldCheckSlop = false
//                Timber.i("onInterceptTouchEvent else")
            }
        }


    @SuppressLint("ClickableViewAccessibility")
    private fun setContainerOnTouchListener() {
        this@CurtainContainerView.setOnTouchListener { containerView, event ->
            if (!curtainEnabled) false
            else {
                var eventConsumed = false
                when (event.actionMasked) {
                    //ACTION_DOWN wan't be called if there are focusable views underneath
                    //and onInterceptTouchEvent ACTION_MOVE didn't return true,
                        // otherwise it will be called
                    MotionEvent.ACTION_DOWN -> if (isTouchAtTopPosition(event)) {
                        animateActionBarViewAlpha(toAlpha = true)
                        setEventProperties(true, event)
                        isEvent = true
                        eventConsumed = true
                    } else if (isTouchAtBottomPosition(event)) {
                        setEventProperties(false, event)
                        isEvent = true
                        eventConsumed = true
                    }
                    MotionEvent.ACTION_MOVE -> if (isEvent) {
//                    Timber.i("ACTION_MOVE:\ncontainer.bottom ${containerView.bottom} | container.top ${containerView.top} | event.y ${event.y} | movingView.height ${curtainView.height} | bottomTouchPosition $bottomTouchPosition | topTouchPosition $topTouchPosition")
                        wasMovingDown = previousPositionY < event.y
                        val currentVelocity = getVelocity(event) ?: 0f
                        if (!isInHighVelocityEvent && isInHighVelocityRange(currentVelocity)) {
                            isInHighVelocityEvent = true
                            lastHighVelocityValue = currentVelocity
                        } else if (isInHighVelocityEvent && !isInHighVelocityRange(currentVelocity)) {
                            isInHighVelocityEvent = false
                        }
                        curtainView.animate()
                            .y(
                                if (isTopToBottom) getYPositionForCurtainMovementFromTopToBottom(
                                    event
                                ) else getYPositionForCurtainMovementFromBottomToTop(
                                    event
                                )
                            ).setDuration(0).start()
                        previousPositionY = event.y
                        eventConsumed = true
                    }
                    MotionEvent.ACTION_UP -> {
//                    Timber.i("ACTION_UP")
                        if (isEvent) {
//                        Timber.i("isEvent")
                            if (isInHighVelocityEvent) {
//                            Timber.i("isInHighVelocityEvent")
                                curtainView.animate()
                                    .y(if (wasMovingDown) getCurtainYBottomMovementPosition() else getCurtainYTopMovementPosition())
                                    .setDuration(
                                        calcAnimationDuration(
                                            isMovingDown = wasMovingDown,
                                            currentBottomPositionY = getCurtainCurrentYBottomPosition(),
                                            isHighVelocity = true
                                        )
                                    ).setAnimationEndListener {
                                        if (!wasMovingDown) animateActionBarViewAlpha(toAlpha = false)
                                    }.start()
                            } else {
                                if (isTopToBottom) {
                                    if (event.y > containerView.bottom * 0.4) {
                                        curtainView.animate()
                                            .y(getCurtainYBottomMovementPosition())
                                            .setDuration(
                                                calcAnimationDuration(
                                                    isMovingDown = true,
                                                    currentBottomPositionY = getCurtainCurrentYBottomPosition()
                                                )
                                            ).start()
                                    } else {
                                        curtainView.animate()
                                            .y(getCurtainYTopMovementPosition())
                                            .setDuration(
                                                calcAnimationDuration(
                                                    isMovingDown = false,
                                                    currentBottomPositionY = getCurtainCurrentYBottomPosition()
                                                )
                                            )
                                            .setAnimationEndListener {
                                                animateActionBarViewAlpha(
                                                    toAlpha = false
                                                )
                                            }
                                            .start()
                                    }
                                } else {
                                    if (getCurtainCurrentYBottomPosition() < containerView.bottom * 0.6) {
                                        curtainView.animate()
                                            .y(getCurtainYTopMovementPosition())
                                            .setDuration(
                                                calcAnimationDuration(
                                                    isMovingDown = false,
                                                    currentBottomPositionY = getCurtainCurrentYBottomPosition()
                                                )
                                            )
                                            .setAnimationEndListener {
                                                animateActionBarViewAlpha(
                                                    toAlpha = false
                                                )
                                            }
                                            .start()
                                    } else {
                                        curtainView.animate()
                                            .y(getCurtainYBottomMovementPosition())
                                            .setDuration(
                                                calcAnimationDuration(
                                                    isMovingDown = true,
                                                    currentBottomPositionY = getCurtainCurrentYBottomPosition()
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
    }

    private fun isTouchAtTopPosition(event: MotionEvent) =
        curtainView.y == -curtainView.height.toFloat() && (event.y < this@CurtainContainerView.top + topOffset)

    private fun isTouchAtBottomPosition(event: MotionEvent) =
        this@CurtainContainerView.bottom.toFloat() == curtainView.y + curtainView.height && event.y > this@CurtainContainerView.bottom - bottomOffset

    private fun isInHighVelocityRange(currentVelocity: Float) =
        currentVelocity.roundToInt().absoluteValue in velocityMinThreshold..velocityMaxThreshold

    private fun getYPositionForCurtainMovementFromTopToBottom(event: MotionEvent) =
        this@CurtainContainerView.top - curtainView.height + min(
            event.y - topTouchPosition,
            curtainView.height.toFloat()
        )

    private fun getYPositionForCurtainMovementFromBottomToTop(event: MotionEvent) =
        this@CurtainContainerView.bottom - curtainView.height - max(
            bottomTouchPosition - event.y,
            0f
        )

    private fun getCurtainYTopMovementPosition() = this.top.toFloat() - curtainView.height
    private fun getCurtainYBottomMovementPosition() = this.top.toFloat()
    private fun getCurtainCurrentYBottomPosition() = curtainView.y + curtainView.height

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
            // A value of 1 provides pixels per millisecond, 1000 provides pixels per second
            computeCurrentVelocity(1000)
            // Log velocity of pixels per second
            // Best practice to use VelocityTrackerCompat where possible.
            val yVelocity = getYVelocity(pointerId)
//            Timber.i("Y velocity: $yVelocity")
            yVelocity
        }

    private fun calcAnimationDuration(
        isMovingDown: Boolean,
        currentBottomPositionY: Float,
        isHighVelocity: Boolean = false
    ) =
        (if (isMovingDown) {
            val dpUntilBottom = max(
                0f,
                resources.pixelToDp(curtainView.height - currentBottomPositionY)
            )
            dpUntilBottom / velocityDpPerMilliSec
        } else {
            val dpUntilTop = resources.pixelToDp(max(0f, currentBottomPositionY))
            dpUntilTop / velocityDpPerMilliSec
        } * if (isHighVelocity) 3 else 5).roundToLong()
//            .also { Timber.i("calcAnimationDuration: $it") }


    private fun animateActionBarViewAlpha(toAlpha: Boolean) {
        if (shouldAlphaAnimateActionBar) {
            actionBarView?.run {
                startAnimation(AlphaAnimation(
                    if (toAlpha) 1f else 0.1f,
                    if (toAlpha) 0.1f else 1f
                ).apply {
                    duration = alphaAnimationDurationMillis
                    fillAfter = true
                })
            }
        }
    }

    private fun setEventProperties(
        isTopToBottom: Boolean,
        event: MotionEvent
    ) {
        this.isTopToBottom = isTopToBottom
        previousPositionY = event.y
        if (isTopToBottom) topTouchPosition = event.y else bottomTouchPosition =
            event.y
        setVelocityTracker(event)
    }

    fun slideUp(isHighVelocity: Boolean) {
        curtainView.animate()
            .y(getCurtainYTopMovementPosition())
            .setDuration(
                calcAnimationDuration(
                    isMovingDown = false,
                    currentBottomPositionY = getCurtainCurrentYBottomPosition(),
                    isHighVelocity = isHighVelocity
                )
            ).setAnimationEndListener {
                animateActionBarViewAlpha(toAlpha = false)
            }.start()
    }

    fun enableCurtain(enable: Boolean) {
        curtainEnabled = enable
    }
}
