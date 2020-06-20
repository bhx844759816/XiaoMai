package com.guangzhida.xiaomai.ui.appointment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fengchen.uistatus.UiStatusController
import com.fengchen.uistatus.annotation.UiStatus
import com.fengchen.uistatus.controller.IUiStatusController
import com.fengchen.uistatus.listener.OnCompatRetryListener
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentSingUpAdapter
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.ui.chat.viewmodel.AllSingUpUserViewModel
import kotlinx.android.synthetic.main.activity_all_sign_up_user_layout.*

/**
 * 已报名列表
 */
class AllSingUpUserActivity : BaseActivity<AllSingUpUserViewModel>() {
    private val mUserList = mutableListOf<ChatUserModel>()
    private val mUserAdapter =
        AppointmentSingUpAdapter(
            mUserList
        )
    private lateinit var mUiStatusController: UiStatusController
    override fun layoutId(): Int = R.layout.activity_all_sign_up_user_layout

    override fun initView(savedInstanceState: Bundle?) {
        mUiStatusController = UiStatusController.get().bind(recyclerView)
        val activityId = intent.getStringExtra("activityId")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mUserAdapter
        mViewModel.getSignUpUserList(activityId)
        mUiStatusController.onCompatRetryListener =
            OnCompatRetryListener { _, _, _, _ ->
                mViewModel.getSignUpUserList(activityId)
            }
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        mViewModel.mChatUserModelObserver.observe(this, Observer {
            if (it.isEmpty()) {
                mUiStatusController.changeUiStatus(UiStatus.EMPTY)
            } else {
                mUiStatusController.changeUiStatus(UiStatus.CONTENT)
                mUserList.clear()
                mUserList.addAll(it)
                mUserAdapter.notifyDataSetChanged()
            }
        })
        mViewModel.mRequestErrorObserver.observe(this, Observer {
            mUiStatusController.changeUiStatus(UiStatus.NETWORK_ERROR)
        })
        mUserAdapter.mContactCallBack = {
            val params = Pair("userName", it.mobilePhone)
            startKtxActivity<ChatMessageActivity>(value = params)
        }
    }
}