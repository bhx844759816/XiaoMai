package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.addTextChangedListener
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.ui.chat.adapter.AddFriendsAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AddFriendsViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_add_friends.*

/**
 * 添加好友界面
 */
class AddFriendsActivity : BaseActivity<AddFriendsViewModel>() {
    private val mListChatUserModel = mutableListOf<ChatUserModel>()

    private val mAdapter by lazy {
        AddFriendsAdapter(mListChatUserModel) {
            addUserFriend(it)
        }
    }

    override fun layoutId(): Int = R.layout.activity_add_friends

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        registerLiveDataObserver()
    }

    override fun initListener() {
        etInputSearch.addTextChangedListener {
            afterTextChanged {

            }
        }
        etInputSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchKey = etInputSearch.text.toString()
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
        mViewModel.addFriend(chatUserModel.id)
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
                ToastUtils.toastShort("添加好友成功")
                finish()
            } else {
                ToastUtils.toastShort("添加好友失败,请稍后再试")
            }
        })
    }
}