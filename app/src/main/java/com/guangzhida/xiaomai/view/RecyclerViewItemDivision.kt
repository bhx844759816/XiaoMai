package com.guangzhida.xiaomai.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewItemDivision(
    private val itemSpace: Int,
    private val itemSpaceBg: Int = Color.WHITE
) : RecyclerView.ItemDecoration() {
    private val mPaint = Paint().apply {
        color = itemSpaceBg
        isDither = true
        isAntiAlias = true

    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = itemSpace
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (index in 0 until parent.childCount) {
            if( parent.getChildAt(index) != null && index != (parent.childCount - 1)){
                drawHorizontalDecoration(c, parent.getChildAt(index))
            }
        }

    }

    private fun drawHorizontalDecoration(
        canvas: Canvas,
        childView: View
    ) {
        val rect = Rect(0, 0, 0, 0)
        rect.top = childView.bottom
        rect.bottom = rect.top + itemSpace
        rect.left = childView.left
        rect.right = childView.right
        canvas.drawRect(rect, mPaint)
    }
}


