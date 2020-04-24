package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.ui.chat.ServiceActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ContactListAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ContactListViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.SwipeItemLayout.OnSwipeItemTouchListener
import com.hyphenate.EMContactListener
import com.hyphenate.chat.EMClient
import kotlinx.android.synthetic.main.fragment_contact_list_layout.*
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 联系人列表
 */
class ContactListFragment : BaseFragment<ContactListViewModel>() {
    private val mChatUserModelList = mutableListOf<UserEntity>()
    //好友监听事件
    private val mContactListener = object : EMContactListener {
        override fun onContactInvited(username: String?, reason: String?) {
            //收到好友邀请
            LogUtils.i("收到好友邀请$username")
        }

        override fun onContactDeleted(username: String?) {
            LogUtils.i("删除了联系人$username")
            viewModel.getContactList()
        }

        override fun onFriendRequestAccepted(username: String?) {
            viewModel.getContactList()
        }

        override fun onContactAdded(username: String?) {
            LogUtils.i("增加了联系人$username")
            viewModel.getContactList()
        }

        override fun onFriendRequestDeclined(username: String?) {
            viewModel.getContactList()
        }
    }

    private val mAdapter by lazy {
        ContactListAdapter(mChatUserModelList)
    }

    override fun layoutId(): Int = R.layout.fragment_contact_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.animationEnable = true
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener { _, _, position ->
            if (mChatUserModelList[position].userName == SERVICE_USERNAME) {
                startActivity(Intent(context, ServiceActivity::class.java))
            } else {
                val intent = Intent(context, ChatMessageActivity::class.java)
                intent.putExtra("userName", mChatUserModelList[position].userName)
                startActivity(intent)
            }
        }
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getContactList()
        }
        EMClient.getInstance().contactManager().setContactListener(mContactListener)
        viewModel.getContactList()
        registerLiveDataObserver()
    }



    private fun registerLiveDataObserver() {
        viewModel.mContactListLiveData2.observe(this, Observer {
            mChatUserModelList.clear()
            mChatUserModelList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })

        viewModel.swipeRefreshResultLiveData.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        EMClient.getInstance().contactManager().removeContactListener(mContactListener)
    }

}