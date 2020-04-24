package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.EaseUiHelper
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.ConversationModelWrap
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.ui.chat.SearchActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ConversationAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ConversationViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import kotlinx.android.synthetic.main.fragment_conversation_layout.*

/**
 * 会话Fragment
 */
class ConversationFragment : BaseFragment<ConversationViewModel>() {
    private val mList = mutableListOf<ConversationModelWrap>()
    private val mAdapter by lazy { ConversationAdapter(mList) }
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

    override fun layoutId(): Int = R.layout.fragment_conversation_layout

    override fun initView(savedInstanceState: Bundle?) {
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.addHeaderView(initTopSearchView())
        mAdapter.animationEnable = true
        recyclerView.adapter = mAdapter
        //下拉刷新样式
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        initLiveDataObserver()
    }

    override fun initListener() {
        //点击跳转到聊天界面
        mAdapter.mClickContentCallBack = {
            val intent = Intent(context, ChatMessageActivity::class.java)
            intent.putExtra("userName", it.conversationEntity?.userName)
            startActivity(intent)
        }
        //长按弹出 /置顶/删除聊天/标记已读/标记未读
        mAdapter.mLongClickContentCallBack = { item, view ->
            showPopupMenu(item, view)
        }
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
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


    /**
     * 初始化数据观察
     */
    private fun initLiveDataObserver() {
        //接收到会话列表
        viewModel.mConversationListLiveData.observe(this, Observer {
            mList.clear()
            mList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
        //刷新回调
        viewModel.mSwipeRefreshLiveData.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
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
    }


    override fun onResume() {
        super.onResume()
        refresh()
        //清除通知栏
        EaseUiHelper.cancelNotifyMessage()
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener)
    }

    private fun refresh() {
        viewModel.loadConversationList()
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
        return if (item.conversationEntity?.isTop == true) {
            arrayOf("取消置顶", "删除该聊天")
        } else {
            arrayOf("置顶", "删除该聊天")
        }
    }

}