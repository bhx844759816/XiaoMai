package com.guangzhida.xiaomai.ui.chat.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.NewFriendsActivity
import com.guangzhida.xiaomai.ui.chat.PersonInfoActivity
import com.guangzhida.xiaomai.ui.chat.SearchActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ContactListAdapter2
import com.guangzhida.xiaomai.ui.chat.viewmodel.ContactListViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.PinyinUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.azlist.AZItemEntity
import com.guangzhida.xiaomai.view.azlist.AZTitleDecoration
import com.guangzhida.xiaomai.view.azlist.AZTitleDecoration2
import com.guangzhida.xiaomai.view.azlist.LettersComparator
import com.hyphenate.EMContactListener
import com.hyphenate.chat.EMClient
import com.hyphenate.util.DateUtils
import kotlinx.android.synthetic.main.fragment_contact_list_layout2.*
import kotlinx.android.synthetic.main.layout_chat_new_friend_apply.*
import kotlinx.android.synthetic.main.layout_chat_search.*
import java.util.*

/**
 * 联系人列表
 *
 */
class ContactListFragment2 : BaseFragment<ContactListViewModel>() {
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

    override fun layoutId(): Int = R.layout.fragment_contact_list_layout2

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.addHeaderView(initTopSearchView(),0)
        mAdapter.addHeaderView(initTopFriendView(),1)
        mAdapter.mContentClickCallBack = {
            startKtxActivity<PersonInfoActivity>(
                values = listOf(
                    Pair("State", 1),
                    Pair("UserEntityGson", Gson().toJson(it))
                )
            )
        }
        recyclerView.adapter = mAdapter
        val azTitleDecorationAttributes = AZTitleDecoration2.TitleAttributes(context).apply {
            setItemHeight(20)
            setTextColor(Color.parseColor("#818181"))
            setTextSize(10)
            setBackgroundColor(Color.parseColor("#fff2f2f2"))
        }
        recyclerView.addItemDecoration(AZTitleDecoration2(azTitleDecorationAttributes, mAdapter.headerLayoutCount))
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getContactList()
        }

        //滚动定位
        bar_list.setOnLetterChangeListener {
            if (it == "↑") {
                scrollToPosition(0)
            } else if (it == "#") {
                LogUtils.i("count=${mAdapter.itemCount}")
                LogUtils.i("headerLayoutCount=${mAdapter.headerLayoutCount}")
                scrollToPosition(mAdapter.itemCount - 1)
            } else {
                val position = mAdapter.getSortLettersFirstPosition(it);
                LogUtils.i("bar_list=$it，position=$position")
                if (position != -1) {
                    scrollToPosition(position + mAdapter.headerLayoutCount)
                }
            }

        }
        EMClient.getInstance().contactManager().setContactListener(mContactListener)
        registerLiveDataObserver()
    }

    private fun scrollToPosition(position: Int) {
        recyclerView.post{
            recyclerView.smoothScrollToPosition(position)
        }
//        if (recyclerView.layoutManager is LinearLayoutManager) {
//            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
//                position,
//                0
//            );
//        } else {
//            (recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(position);
//        }
    }

    /**
     * 初始化顶部搜索界面
     */
    private fun initTopSearchView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_chat_search, null)
        view.setOnClickListener {
            startKtxActivity<SearchActivity>()
        }
        return view
    }

    /**
     * 初始化顶部搜索界面
     */
    private fun initTopFriendView(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_chat_new_friend_apply, null)
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

    /**
     * 懒加载数据
     */
    override fun lazyLoadData() {
        viewModel.getContactList()
    }

    /**
     * 注册观察者
     */
    private fun registerLiveDataObserver() {
        //接收到好友列表
        viewModel.mContactListLiveData2.observe(this, Observer {
            val list = fillData(it)
            Collections.sort(list, LettersComparator())
            mChatUserModelList.clear()
            mChatUserModelList.addAll(list)
            mAdapter.notifyDataSetChanged()
        })
        //刷新完成
        viewModel.swipeRefreshResultLiveData.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
        })
        //收到好友请求
        viewModel.mInviteMessageEntityListLiveData.observe(this, Observer {
            if (it.isNotEmpty()) {
                tvName?.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    this.topToTop = R.id.ivHeaderView
                    this.leftToRight = R.id.ivHeaderView
                    this.topMargin = context?.dp2px(3) ?: 6
                    this.leftMargin = context?.dp2px(15) ?: 15
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
                    this.leftMargin = context?.dp2px(15) ?: 15
                }
                tvChatUnReadMessageCount?.gone()
                tvChatMessage?.gone()
                tvTime?.gone()
            }
        })
    }

    private fun refresh() {
        viewModel.getContactList()
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

    override fun onDestroy() {
        super.onDestroy()
        EMClient.getInstance().contactManager().removeContactListener(mContactListener)
    }

}