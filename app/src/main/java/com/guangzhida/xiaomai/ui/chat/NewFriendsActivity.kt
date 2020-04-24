package com.guangzhida.xiaomai.ui.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.addFriendChangeLiveData
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivityForResult
import com.guangzhida.xiaomai.model.NewFriendModel
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.ui.chat.adapter.NewFriendsAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.NewFriendsViewModel
import kotlinx.android.synthetic.main.activity_new_friends_layout.*

/**
 * 新朋友界面
 */
class NewFriendsActivity : BaseActivity<NewFriendsViewModel>() {
    private val mNewFriendsList = mutableListOf<NewFriendModel>()
    private val mAdapter by lazy {
        NewFriendsAdapter(mNewFriendsList)
    }

    override fun layoutId(): Int = R.layout.activity_new_friends_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initLiveDataObserver()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        mViewModel.loadNewFriends()
    }

    override fun initListener() {
        //同意好友申请
        mAdapter.mAgreeCallBack = {
            mViewModel.agreeFriend(it.userEntity.uid.toString(), it.inviteMessageEntity.from)
        }
        //点击Item跳转到好友详情界面
        mAdapter.mItemClickCallBack = {
            startKtxActivity<PersonInfoActivity>(
                values = listOf(
                    Pair("State", 0),
                    Pair("UserEntityGson", Gson().toJson(it.userEntity))
                )
            )
        }
        toolBar.setNavigationOnClickListener {
            finish()
        }
        tvAddFriend.clickN {
            startKtxActivityForResult<AddFriendsActivity>(requestCode = ADD_FRIEND_CODE)
        }
    }

    /**
     * 初始化观察者
     */
    private fun initLiveDataObserver() {
        mViewModel.mNewFriendModeLiveData.observe(this, Observer {
            mNewFriendsList.clear()
            mNewFriendsList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
        //刷新界面显示
        mViewModel.operateResultLiveData.observe(this, Observer {
            mViewModel.loadNewFriends()
        })
        //接收到主动添加新朋友
        addFriendChangeLiveData.observe(this, Observer {
            mViewModel.loadNewFriends()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_FRIEND_CODE && resultCode == Activity.RESULT_OK) {
            mViewModel.loadNewFriends()
        }
    }

    companion object {
        const val ADD_FRIEND_CODE = 0x01
    }
}