package com.guangzhida.xiaomai.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(space: Int, column: Int) : RecyclerView.ItemDecoration() {

    private val mSpace = space
    private val mColumn = column
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        //不是第一个的格子都设一个左边和底部的间距
        outRect.left = mSpace;
        outRect.bottom = mSpace;
        //由于每行都只有column个，所以第一个都是column的倍数，把左边距设为0
        if (parent.getChildLayoutPosition(view) % mColumn == 0) {
            outRect.left = 0;
        }
    }
}