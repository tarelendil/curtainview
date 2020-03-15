package com.triosoft.curtainview.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
/**
 * Created by ${StasK}
 */
class CurtainView : LinearLayout {


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
//        val commonAttributes: TypedArray = context.obtainStyledAttributes(
//            attrs,
//            R.styleable.MenuRowView
//        )
//        val drawableRes = commonAttributes.getResourceId(R.styleable.MenuRowView_mr_icon, 0)
//        val strRes = commonAttributes.getResourceId(R.styleable.MenuRowView_mr_text_res, 0)
//        val showBadge = commonAttributes.getBoolean(R.styleable.MenuRowView_mr_show_badge, false)
//        @ColorRes val textColor = commonAttributes.getResourceId(R.styleable.MenuRowView_mr_text_color, 0)
//
//
//        initView(drawableRes, strRes, showBadge, textColor)
//        commonAttributes.recycle()
    }

    fun initView(
        @DrawableRes drawableRes: Int = 0, @StringRes strRes: Int = 0, showBadge: Boolean = false,
        @ColorRes textColor: Int = 0
    ) {
//        binding = DataBindingUtil.inflate(
//            LayoutInflater.from(context),
//            R.layout.row_main_menu,
//            this,
//            true
//        )
//
//        if (drawableRes != 0) {
//            binding.ivIcon.setImageResource(drawableRes)
//        } else {
//            binding.ivIcon.visibility = View.INVISIBLE
//        }
//
//        if (strRes != 0) {
//            binding.tvTitle.setText(strRes)
//        }
//        if(textColor != 0) binding.tvTitle.setTextColor(ContextCompat.getColor(binding.tvTitle.context, textColor))
//
//        if (showBadge) binding.tvBadge.visibility = View.VISIBLE
    }

}