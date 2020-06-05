package com.guangzhida.xiaomai.view

import android.content.Context
import android.util.AttributeSet
import android.widget.AbsListView
import androidx.core.view.ViewCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class XSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) :
    SwipeRefreshLayout(context, attributeSet) {

    override fun canChildScrollUp(): Boolean {

        val target = getChildAt(0);
        return if (target is AbsListView) {
            val absListView = target as AbsListView
            (target.childCount > 0
                    && (target.firstVisiblePosition > 0 || absListView.getChildAt(0)
                .top < target.getPaddingTop()));
        } else
            ViewCompat.canScrollVertically(target,-1);
    }

}
