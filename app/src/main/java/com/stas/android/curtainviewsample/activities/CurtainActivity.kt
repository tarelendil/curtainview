package com.stas.android.curtainviewsample.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.stas.android.curtainviewsample.R

class CurtainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
    }

    @VisibleForTesting
    fun getLayoutId() = R.layout.activity_test
}
