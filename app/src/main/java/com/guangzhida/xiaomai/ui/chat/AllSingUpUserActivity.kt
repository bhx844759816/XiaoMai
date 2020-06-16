package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.ui.chat.adapter.AppointmentSingUpAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AllSingUpUserViewModel
import kotlinx.android.synthetic.main.activity_all_sign_up_user_layout.*

/**
 * 已报名列表
 */
class AllSingUpUserActivity : BaseActivity<AllSingUpUserViewModel>() {
    private val mUserList = mutableListOf<ChatUserModel>()
    private val mUserAdapter = AppointmentSingUpAdapter(mUserList)
    override fun layoutId(): Int = R.layout.activity_all_sign_up_user_layout

    override fun initView(savedInstanceState: Bundle?) {
        val activityId = intent.getStringExtra("activityId")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mUserAdapter
        mViewModel.getSignUpUserList(activityId)
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        mViewModel.mChatUserModelObserver.observe(this, Observer {
            mUserList.clear()
            mUserList.addAll(it)
            mUserAdapter.notifyDataSetChanged()
        })
        mUserAdapter.mContactCallBack = {
            val params = Pair("userName", it.mobilePhone)
            startKtxActivity<ChatMessageActivity>(value = params)
        }
    }
}