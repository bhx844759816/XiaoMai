package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.addTextChangedListener
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.hideKeyboard
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.adapter.SearchContactListAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.SearchContactListViewModel
import kotlinx.android.synthetic.main.activity_search_contact_list_layout.*

/**
 * 搜索详情列表
 */
class SearchContactListActivity : BaseActivity<SearchContactListViewModel>() {
    private var mSearchKey: String = ""
    private val mUserEntityList = mutableListOf<UserEntity>()
    private val mAdapter by lazy {
        SearchContactListAdapter(mUserEntityList)
    }

    override fun layoutId(): Int = R.layout.activity_search_contact_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        mSearchKey = intent.getStringExtra("SearchKey")
        mAdapter.mContentClickCallBack = {
            startKtxActivity<PersonInfoActivity>(
                values = listOf(
                    Pair("State", 1),
                    Pair("UserEntityGson", Gson().toJson(it))
                )
            )
        }
        mAdapter.addHeaderView(getTopItemView())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        etInput.addTextChangedListener {
            afterTextChanged {
                mSearchKey = it?.toString() ?: ""
                if (mSearchKey.isNotEmpty()) {
                    mAdapter.mKey = mSearchKey
                    mViewModel.doSearchContactList(mSearchKey)
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

    override fun initListener() {

    }

    /**
     * 获取顶部的view
     */
    private fun getTopItemView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_chat_search_top, null)
        val textView = view.findViewById<TextView>(R.id.tvTitle)
        textView.text = "联系人"
        return view
    }

    /**
     * 注册观察者
     */
    private fun initObserver() {
        mViewModel.searchUserEntityList.observe(this, Observer {
            mUserEntityList.clear()
            mUserEntityList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
    }
}