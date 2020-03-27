package com.guangzhida.xiaomai.ui.chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ui.chat.viewmodel.ConversationViewModel
import com.guangzhida.xiaomai.ui.chat.adapter.ConversationAdapter
import com.guangzhida.xiaomai.view.SwipeItemLayout
import kotlinx.android.synthetic.main.fragment_conversation_layout.*

/**
 * 会话Fragment
 */
class ConversationFragment : BaseFragment<ConversationViewModel>() {
    private val mList = mutableListOf("", "", "")
    private val mAdapter by lazy { ConversationAdapter(mList) }
    override fun layoutId(): Int = R.layout.fragment_conversation_layout


    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(context))
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));//设置分割线
        mAdapter.animationEnable = true
        mAdapter.addHeaderView(getHeaderView(), 0)
        recyclerView.adapter = mAdapter
    }

    private fun getHeaderView(): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_chat_query_friends, recyclerView, false)
    }

    /**
     * 获取分割线
     *
     * @param drawableId 分割线id
     * @return
     */
    private fun getRecyclerViewDivider(@DrawableRes drawableId: Int): RecyclerView.ItemDecoration {
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(resources.getDrawable(drawableId))
        return itemDecoration
    }
}