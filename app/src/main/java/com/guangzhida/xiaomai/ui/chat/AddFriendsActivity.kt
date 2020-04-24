package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.addFriendChangeLiveData
import com.guangzhida.xiaomai.ext.addTextChangedListener
import com.guangzhida.xiaomai.ktxlibrary.ext.hideKeyboard
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.adapter.AddFriendsAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AddFriendsViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.chat.EMClient
import kotlinx.android.synthetic.main.activity_add_friends.*

/**
 * 添加好友界面
 */
class AddFriendsActivity : BaseActivity<AddFriendsViewModel>() {
    private val mListChatUserModel = mutableListOf<ChatUserModel>()

    private val mAdapter by lazy {
        AddFriendsAdapter(mListChatUserModel)
    }

    override fun layoutId(): Int = R.layout.activity_add_friends

    override fun initView(savedInstanceState: Bundle?) {
        mAdapter.mCallBack = {
            ToastUtils.toastShort("添加好友")
            if (BaseApplication.instance().mUserModel != null && BaseApplication.instance().mUserModel!!.id != it.id) {
                addUserFriend(it)
            } else {
                ToastUtils.toastShort("不能添加自己为好友")
            }
        }
        mAdapter.mContentClickCallBack = {
            val userEntity = UserEntity(it.id.toLong()).apply {
                userName = it.mobilePhone
                nickName = it.nickName
                avatarUrl = it.headUrl ?: ""
                age = it.age.toString()
                sex = it.sex.toString()
                singUp = it.signature ?: ""
            }
            startKtxActivity<PersonInfoActivity>(
                values = listOf(
                    Pair("State", 0),
                    Pair("UserEntityGson", Gson().toJson(userEntity))
                )
            )
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        registerLiveDataObserver()
    }

    override fun initListener() {
        tvCancel.setOnClickListener {
            hideKeyboard()
            finish()
        }
        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchKey = etInput.text.toString()
                if (searchKey.isNotEmpty()) {
                    mViewModel.doSearch(searchKey)
                    return@setOnEditorActionListener true
                } else {
                    ToastUtils.toastShort("请输入搜索关键词")
                }
            }
            return@setOnEditorActionListener false
        }
    }

    /**
     * 添加好友
     */
    private fun addUserFriend(chatUserModel: ChatUserModel) {
        mViewModel.addFriend(chatUserModel)
    }

    /**
     * 注册数据观察者
     */
    private fun registerLiveDataObserver() {
        //获取搜索结果
        mViewModel.mSearchResultLiveData.observe(this, Observer {
            mListChatUserModel.clear()
            mListChatUserModel.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
        //添加好友
        mViewModel.mAddFriendLiveData.observe(this, Observer {
            if (it) {
                addFriendChangeLiveData.postValue(true)
                ToastUtils.toastShort("验证信息发送成功")
                finish()
            } else {
                ToastUtils.toastShort("验证信息发送失败,请稍后再试")
            }
        })
    }
}