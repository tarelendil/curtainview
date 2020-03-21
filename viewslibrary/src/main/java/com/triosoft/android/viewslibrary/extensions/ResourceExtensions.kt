package com.triosoft.android.viewslibrary.extensions

import android.content.res.Resources
import android.util.DisplayMetrics


fun Resources.pixelToDp(pixels: Float) =
    pixels / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)