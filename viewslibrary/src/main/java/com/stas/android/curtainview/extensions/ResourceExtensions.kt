package com.stas.android.curtainview.extensions

import android.content.res.Resources
import android.util.DisplayMetrics


fun Resources.pixelToDp(pixels: Float) =
    pixels / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)