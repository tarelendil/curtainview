package com.triosoft.curtainview

import android.content.res.Resources
import android.util.TypedValue

/**
 * Created by ${StasK} isOn 14/03/2019.
 */
private fun Resources.getPixelSize(dpSize: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, displayMetrics)

