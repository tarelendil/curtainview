package com.triosoft.viewslibrary.extensions

import android.animation.Animator
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewPropertyAnimator


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