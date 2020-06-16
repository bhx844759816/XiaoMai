package com.guangzhida.xiaomai.ui.chat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.adapter.ContactListAdapter2
import com.guangzhida.xiaomai.ui.chat.viewmodel.ContactListViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.PinyinUtils
import com.guangzhida.xiaomai.view.azlist.AZItemEntity
import com.guangzhida.xiaomai.view.azlist.AZTitleDecoration2
import com.guangzhida.xiaomai.view.azlist.LettersComparator
import com.hyphenate.EMContactListener
import com.hyphenate.chat.EMClient
import com.hyphenate.util.DateUtils
import kotlinx.android.synthetic.main.activity_contact_list_layout.*
import java.util.*

class ContactListActivity : BaseActivity<ContactListViewModel>() {

    //联系人列表
    private val mChatUserModelList = mutableListOf<AZItemEntity<UserEntity>>()
    private val mAdapter by lazy {
        ContactListAdapter2(mChatUserModelList)
    }
    private var ivHeaderView: ImageView? = null
    private var tvName: TextView? = null
    private var tvChatMessage: TextView? = null
    private var tvChatUnReadMessageCount: TextView? = null
    private var tvTime: TextView? = null
    //好友监听事件
    private val mContactListener = object : EMContactListener {
        override fun onContactInvited(username: String, reason: String) {
            //收到好友邀请
            LogUtils.i("收到好友邀请:userName=$username,reason$reason")
            refresh()
        }

        override fun onContactDeleted(username: String) {
            LogUtils.i("删除了联系人$username")
            refresh()
        }

        override fun onFriendRequestAccepted(username: String) {
            LogUtils.i("接收了好友请求$username")
            refresh()
        }

        override fun onContactAdded(username: String) {
            LogUtils.i("增加了联系人$username")
            refresh()
        }

        override fun onFriendRequestDeclined(username: String) {
            LogUtils.i("好友请求被拒绝$username")
            refresh()
        }
    }

    override fun layoutId(): Int = R.layout.activity_contact_list_layout

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.addHeaderView(initTopSearchView(), 0)
        mAdapter.addHeaderView(initTopFriendView(), 1)
        mAdapter.mContentClickCallBack = {
            startKtxActivity<PersonInfoActivity>(value = Pair("userName", it.userName))
        }
        recyclerView.adapter = mAdapter
        val azTitleDecorationAttributes = AZTitleDecoration2.TitleAttributes(this).apply {
            setItemHeight(20)
            setTextColor(Color.parseColor("#818181"))
            setTextSize(10)
            setBackgroundColor(Color.parseColor("#fff2f2f2"))
        }
        recyclerView.addItemDecoration(
            AZTitleDecoration2(
                azTitleDecorationAttributes,
                mAdapter.headerLayoutCount
            )
        )
        //滚动定位
        bar_list.setOnLetterChangeListener {
            if (it == "↑") {
                scrollToPosition(0)
            } else if (it == "#") {
                scrollToPosition(mAdapter.itemCount - 1)
            } else {
                val position = mAdapter.getSortLettersFirstPosition(it);
                if (position != -1) {
                    scrollToPosition(position + mAdapter.headerLayoutCount)
                }
            }
        }
        EMClient.getInstance().contactManager().setContactListener(mContactListener)
        registerLiveDataObserver()
        refresh()
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        rlAddFriend.clickN {
            startKtxActivity<AddFriendsActivity>()
        }
    }

    /**
     * 初始化顶部搜索界面
     */
    private fun initTopSearchView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_chat_search, null)
        view.setOnClickListener {
            startKtxActivity<SearchActivity>()
        }
        return view
    }

    /**
     * 初始化顶部搜索界面
     */
    private fun initTopFriendView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_chat_new_friend_apply, null)
        ivHeaderView = view.findViewById(R.id.ivHeaderView)
        tvName = view.findViewById(R.id.tvName)
        tvChatMessage = view.findViewById(R.id.tvChatMessage)
        tvChatUnReadMessageCount = view.findViewById(R.id.tvChatUnReadMessageCount)
        tvTime = view.findViewById(R.id.tvTime)
        view.findViewById<ConstraintLayout>(R.id.newFriendParent).clickN {
            startKtxActivity<NewFriendsActivity>()
        }
        return view
    }

    private fun refresh() {
        mViewModel.getContactList()
    }

    /**
     * 填充数据
     */
    private fun fillData(date: List<UserEntity>): List<AZItemEntity<UserEntity>> {
        val sortList = mutableListOf<AZItemEntity<UserEntity>>()
        for (aDate in date) {
            val item = AZItemEntity<UserEntity>()
            item.value = aDate
            //汉字转换成拼音
            val pinyin: String = PinyinUtils.getPingYin(aDate.nickName)
            //取第一个首字母
            val letters = pinyin.substring(0, 1).toUpperCase()
            // 正则表达式，判断首字母是否是英文字母
            if (letters.matches(Regex("[A-Z]"))) {
                item.setSortLetters(letters.toUpperCase())
            } else {
                item.setSortLetters("#")
            }
            sortList.add(item)
        }
        return sortList
    }

    /**
     * 注册观察者
     */
    private fun registerLiveDataObserver() {
        mViewModel.mContactListLiveData2.observe(this, Observer {
            val list = fillData(it)
            Collections.sort(list, LettersComparator())
            mChatUserModelList.clear()
            mChatUserModelList.addAll(list)
            mAdapter.notifyDataSetChanged()
        })
        //收到好友请求
        mViewModel.mInviteMessageEntityListLiveData.observe(this, Observer {
            if (it.isNotEmpty()) {
                tvName?.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topToTop = R.id.ivHeaderView
                    this.leftToRight = R.id.ivHeaderView
                    this.topMargin = dp2px(3) ?: 6
                    this.leftMargin = dp2px(15) ?: 15
                }
                tvChatUnReadMessageCount?.visible()
                tvChatMessage?.visible()
                tvTime?.visible()
                tvChatUnReadMessageCount?.text = it.size.toString()
                tvTime?.text = DateUtils.getTimestampString(Date(it.last().time))
                tvChatMessage?.text = buildString {
                    append(it.last().nickName)
                    append("请求添加好友")
                }
            } else {
                tvName?.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topToTop = R.id.ivHeaderView
                    this.bottomToBottom = R.id.ivHeaderView
                    this.leftToRight = R.id.ivHeaderView
                    this.leftMargin = dp2px(15) ?: 15
                }
                tvChatUnReadMessageCount?.gone()
                tvChatMessage?.gone()
                tvTime?.gone()
            }
        })
    }

    private fun scrollToPosition(position: Int) {
        recyclerView.post {
            recyclerView.smoothScrollToPosition(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EMClient.getInstance().contactManager().removeContactListener(mContactListener)
    }
}