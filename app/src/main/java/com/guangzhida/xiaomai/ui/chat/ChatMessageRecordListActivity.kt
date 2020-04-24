package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.os.Parcelable
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
import com.guangzhida.xiaomai.model.ChatMessageRecordModel
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMessageRecordAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatMessageRecordListViewModel
import kotlinx.android.synthetic.main.activity_chat_message_record_list_layout.*
import java.io.Serializable

/**
 * 聊天记录列表
 */
class ChatMessageRecordListActivity : BaseActivity<ChatMessageRecordListViewModel>() {
    private val mChatMessageRecordModelList = mutableListOf<ChatMessageRecordModel>()
    private val mAdapter by lazy {
        ChatMessageRecordAdapter(mChatMessageRecordModelList)
    }
    private var mSearchKey: String = ""
    private var mUserName: String = ""
    private var mNickName: String = ""

    override fun layoutId(): Int = R.layout.activity_chat_message_record_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        mSearchKey = intent.getStringExtra("SearchKey")
        mUserName = intent.getStringExtra("UserName")
        mNickName = intent.getStringExtra("NickName")
        mAdapter.addHeaderView(getTopItemView())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        tvCancel.clickN {
            hideKeyboard()
            finish()
        }
        etInput.addTextChangedListener {
            afterTextChanged {
                mSearchKey = it?.toString() ?: ""
                if (mSearchKey.isNotEmpty()) {
                    mViewModel.queryChatMessageRecord(mSearchKey, mUserName)
                }
            }
        }
        //点击指定的聊天Item
        mAdapter.mContentClickCallBack = {
            mViewModel.queryChatMessageByMsgId(it.atTime, mUserName)
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
        textView.text = buildString {
            append("\"")
            append(mNickName)
            append("\"的聊天记录")
        }
        return view
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        //获取搜索结果
        mViewModel.mChatMessageRecordModelList.observe(this, Observer {
            val sortList = it.sortedBy { model ->
                model.atTime
            }.asReversed()
            mChatMessageRecordModelList.clear()
            mChatMessageRecordModelList.addAll(sortList)
            mAdapter.notifyDataSetChanged()
        })
        //获取到此条消息后所有的消息
        mViewModel.mQueryChatMessageRecord.observe(this, Observer {
            startKtxActivity<ChatMessageActivity>(
                values = listOf(
                    Pair("userName", mUserName), Pair("EMMessageList", it as Serializable),
                    Pair("State", 1)
                )
            )
        })
    }
}