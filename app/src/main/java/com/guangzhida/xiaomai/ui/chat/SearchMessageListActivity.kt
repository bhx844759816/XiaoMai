package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.addTextChangedListener
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.hideKeyboard
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.SearchMessageModel
import com.guangzhida.xiaomai.ui.chat.adapter.SearchMessageListAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.SearchMessageListViewModel
import kotlinx.android.synthetic.main.activity_search_message_list_layout.*

class SearchMessageListActivity : BaseActivity<SearchMessageListViewModel>() {
    private var mSearchKey: String = ""
    private val mSearchMessageModelList = mutableListOf<SearchMessageModel>()
    private val mAdapter by lazy {
        SearchMessageListAdapter(mSearchMessageModelList)
    }

    override fun layoutId(): Int = R.layout.activity_search_message_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        mSearchKey = intent.getStringExtra("SearchKey")
        //mState ==0 搜联系人 mState == 1搜聊天记录
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.addHeaderView(getTopItemView())
        recyclerView.adapter = mAdapter
        //跳转到聊天记录列表详情
        mAdapter.mContentClickCallBack = {
            val params = listOf(
                Pair("SearchKey", mSearchKey),
                Pair("UserName", it.userEntity.userName),
                Pair("NickName", it.userEntity.nickName)
            )
            startKtxActivity<ChatMessageRecordListActivity>(values = params)
        }
        etInput.addTextChangedListener {
            afterTextChanged {
                mSearchKey = it?.toString() ?: ""
                if (mSearchKey.isNotEmpty()) {
                    mViewModel.doSearchChatMessage(mSearchKey)
                }
            }
        }
        tvCancel.clickN {
            hideKeyboard()
            finish()
        }
        etInput.setText(mSearchKey)
        etInput.setSelection(mSearchKey.length)
        initObserver()
    }
    /**
     * 获取顶部的view
     */
    private fun getTopItemView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_chat_search_top, null)
        val textView = view.findViewById<TextView>(R.id.tvTitle)
        textView.text = "聊天记录"
        return view
    }

    /**
     * 注册观察者
     */
    private fun initObserver() {
        //搜索聊天记录
        mViewModel.searchChatMessageMap.observe(this, Observer {
            val list = it.entries.map { map ->
                map.value
            }
            mSearchMessageModelList.clear()
            mSearchMessageModelList.addAll(list)
            mAdapter.notifyDataSetChanged()
        })
    }
}