package com.stas.android.curtainviewsample.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.stas.android.curtainview.views.curtain.CurtainContainerView
import com.stas.android.curtainviewsample.R
import com.stas.android.curtainviewsample.databinding.ActivityCurtainBinding

class CurtainActivity : AppCompatActivity() {

    private lateinit var activityCurtainBinding: ActivityCurtainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCurtainBinding = ActivityCurtainBinding.inflate(layoutInflater)
        setContentView(activityCurtainBinding.root)
    }

    @VisibleForTesting
    fun getLayoutId() = R.layout.activity_curtain

    companion object {
        const val WITH_ACTION_BAR = "WITH_ACTION_BAR"
    }

    fun slideUp(view: View) {
        activityCurtainBinding.containerView.slideUp(true)
    }
}
