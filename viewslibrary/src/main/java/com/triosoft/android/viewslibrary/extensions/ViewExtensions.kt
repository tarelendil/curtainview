package com.triosoft.android.viewslibrary.extensions

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver


fun ViewPropertyAnimator.setAnimationEndListener(action: () -> Unit): ViewPropertyAnimator =
    setListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            setListener(null)
            action()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
    })

fun View.setOneTimeGlobalLayoutObserver(action: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    })
}

fun View.setGlobalLayoutObserver(action: () -> Boolean) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (action()) viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}