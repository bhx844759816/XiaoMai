package com.guangzhida.xiaomai.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class VerifyInternetViewGroup constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int
) : FrameLayout(context, attributeSet, defStyleAttr) {


    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(1,1)
    }

}