package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ContactListAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ContactListViewModel
import com.guangzhida.xiaomai.view.SwipeItemLayout.OnSwipeItemTouchListener
import kotlinx.android.synthetic.main.fragment_contact_list_layout.*

/**
 * 联系人列表
 */
class ContactListFragment : BaseFragment<ContactListViewModel>() {
    private val mChatUserModelList = mutableListOf<ChatUserModel>()
    private val mAdapter by lazy {
        ContactListAdapter(mChatUserModelList)
    }

    override fun layoutId(): Int = R.layout.fragment_contact_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.animationEnable = true
        mAdapter.addHeaderView(getHeaderView(), 0)
        recyclerView.adapter = mAdapter
        recyclerView.addOnItemTouchListener(OnSwipeItemTouchListener(context))
        mAdapter.setOnItemClickListener { adapter, view, position ->
            startActivity(Intent(context, ChatMessageActivity::class.java))
        }
        viewModel.getContactList()
        registerLiveDataObserver()
    }

    private fun getHeaderView(): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_chat_query_friends, recyclerView, false)
    }

    private fun registerLiveDataObserver() {
        viewModel.mContactListLiveData.observe(this, Observer {
            mChatUserModelList.clear()
            mChatUserModelList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
    }

}