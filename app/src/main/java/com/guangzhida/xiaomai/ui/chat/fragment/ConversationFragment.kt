package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.fengchen.uistatus.UiStatusController
import com.fengchen.uistatus.annotation.UiStatus
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.ConversationModelWrap
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.ui.chat.ChatServiceActivity
import com.guangzhida.xiaomai.ui.chat.SearchActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ConversationAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ConversationViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.NetworkUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.fragment_conversation_layout.*

/**
 * 会话Fragment
 */
class ConversationFragment : BaseFragment<ConversationViewModel>() {
    private val mList = mutableListOf<ConversationModelWrap>()
    private val mAdapter by lazy {
        ConversationAdapter(mList).apply {
            animationEnable = false
        }
    }
    private lateinit var mUiStatusController: UiStatusController
    private val mMessageListener = object : EMMessageListener {
        override fun onMessageRecalled(messages: MutableList<EMMessage>?) {

        }

        override fun onMessageChanged(message: EMMessage?, change: Any?) {
        }

        override fun onCmdMessageReceived(messages: MutableList<EMMessage>?) {


        }

        override fun onMessageReceived(messages: MutableList<EMMessage>) {
            refresh()
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {
        }
    }
    private var mNeedRefresh = false //控制列表是否需要刷新 只有点击聊天的时候在onResume的时候刷新
    private var mErrorView: View? = null
    override fun layoutId(): Int = R.layout.fragment_conversation_layout

    override fun initView(savedInstanceState: Bundle?) {
        mUiStatusController = UiStatusController.get().bind(recyclerView)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.addHeaderView(initTopSearchView())
        recyclerView.adapter = mAdapter
        //下拉刷新样式
        initLiveDataObserver()
        if (BaseApplication.instance().mUserModel == null) {
            mUiStatusController.changeUiStatus(UiStatus.NOT_FOUND)
        } else {
            refresh()
        }
    }


    override fun initListener() {
        //点击跳转到聊天界面
        mAdapter.mClickContentCallBack = {
            mNeedRefresh = true
            //不是客服聊天的话跳转到ChatMessageActivity
            if (it.conversationEntity.type != 1) {
                val params = Pair("userName", it.conversationEntity.userName)
                startKtxActivity<ChatMessageActivity>(value = params)
            } else {
                val serviceModel = ServiceModel(
                    nickName = it.conversationEntity.nickName,
                    headUrl = it.conversationEntity.avatarUrl,
                    sex = Integer.parseInt(it.conversationEntity.sex ?: "0"),
                    age = Integer.parseInt(it.conversationEntity.age ?: "0"),
                    username = it.conversationEntity.userName
                )
                startKtxActivity<ChatServiceActivity>(
                    value = Pair(
                        "serviceModel",
                        Gson().toJson(serviceModel)
                    )
                )
            }
        }
        //长按弹出 /置顶/删除聊天/标记已读/标记未读
        mAdapter.mLongClickContentCallBack = { item, view ->
            showPopupMenu(item, view)
        }
        //下拉刷新
        smartRefreshLayout.setOnRefreshListener {
            refresh()
        }

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

    private fun initErrorConnectView(): View {
        mErrorView =
            LayoutInflater.from(context).inflate(R.layout.view_im_disconnect_widget_layout, null)
        val tvWarning = mErrorView?.findViewById<TextView>(R.id.tvWarning)
        if (context != null && NetworkUtils.isConnected(context)) {
            tvWarning?.text = "和服务器断开连接"
        } else {
            tvWarning?.text = "网络连接出错，请检查网络设置"
            mErrorView!!.setOnClickListener {
                startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
            }
        }
        return mErrorView!!
    }


    /**
     * 初始化数据观察
     */
    private fun initLiveDataObserver() {
        //用户切换
        userModelChangeLiveData.observe(this, Observer {
            if (BaseApplication.instance().mUserModel != null) {
                mList.clear()
                mAdapter.notifyDataSetChanged()
                refresh()
            } else {
                mUiStatusController.changeUiStatus(UiStatus.NOT_FOUND)
            }
        })
        //接收到会话列表
        viewModel.mConversationListLiveData.observe(this, Observer {
            if (it.isNotEmpty()) {
                mUiStatusController.changeUiStatus(UiStatus.CONTENT)
                mList.clear()
                mList.addAll(it)
                mAdapter.notifyDataSetChanged()
            } else {
                mUiStatusController.changeUiStatus(UiStatus.EMPTY)
            }

        })
        //刷新回调
        viewModel.mSwipeRefreshLiveData.observe(this, Observer {
            smartRefreshLayout.finishRefresh()
            mNeedRefresh = false
        })
        //删除会话结果
        viewModel.deleteConversationResult.observe(this, Observer {
            if (it != null) {
                mList.remove(it)
                mAdapter.notifyDataSetChanged()
            }
        })
        //置顶会话的结果
        viewModel.topConversationResult.observe(this, Observer {
            if (it) {
                refresh()
            }
        })
        //
        LiveDataBus.with(LiveDataBusKey.IM_DISCONNECT_SERVER_KEY, Boolean::class.java).observe(this,
            Observer {
                if (mErrorView == null) {
                    mAdapter.addHeaderView(initErrorConnectView(), 0)
                }
            })
        LiveDataBus.with(LiveDataBusKey.IM_CONNECT_SERVER_KEY, Boolean::class.java).observe(this,
            Observer {
                mErrorView?.let {
                    mAdapter.removeHeaderView(it)
                    mErrorView = null
                }
            })
    }

    override fun onResume() {
        super.onResume()
        //清除通知栏
        ChatHelper.cancelNotifyMessage()
        if (BaseApplication.instance().mUserModel != null && mNeedRefresh) {
            refresh()
        }
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener)
    }

    private fun refresh() {
        viewModel.loadConversation()
    }

    override fun onPause() {
        super.onPause()
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener)
    }

    /**
     * 展示长按的PopupMenu
     */
    private fun showPopupMenu(item: ConversationModelWrap, view: View) {
        XPopup.Builder(context)
            .atView(view)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .hasShadowBg(false)
            .isCenterHorizontal(true)
            .asAttachList(getPopupArrayList(item), null) { position, _ ->
                when (position) {
                    0 -> { //置顶 或取消置顶
                        viewModel.makeConversationTop(item)
                    }
                    1 -> {//删除该聊天
                        viewModel.deleteConversation(item)
                    }
                }
            }
            .show()
    }

    private fun getPopupArrayList(item: ConversationModelWrap): Array<String> {
        return if (item.conversationEntity.isTop) {
            arrayOf("取消置顶", "删除该聊天")
        } else {
            arrayOf("置顶", "删除该聊天")
        }
    }

}