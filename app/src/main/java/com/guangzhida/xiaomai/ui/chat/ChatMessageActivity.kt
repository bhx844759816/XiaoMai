package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMessageAdapter
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatMessageViewHolder
import com.guangzhida.xiaomai.utils.AdapterUtils
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.view.chat.DeleteEmoticon
import com.guangzhida.xiaomai.view.chat.EmojiFilter
import com.guangzhida.xiaomai.view.chat.PlaceHoldEmoticon
import com.guangzhida.xiaomai.view.chat.SimpleAppsGridView
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import github.ll.emotionboard.data.Emoticon
import github.ll.emotionboard.interfaces.OnEmoticonClickListener
import github.ll.emotionboard.utils.EmoticonsKeyboardUtils
import github.ll.emotionboard.widget.EmoticonsEditText.OnSizeChangedListener
import github.ll.emotionboard.widget.FuncLayout
import kotlinx.android.synthetic.main.activity_chat_message.*


/**
 * 聊天界面
 */
class ChatMessageActivity : BaseActivity<ChatMessageViewHolder>(), FuncLayout.FuncKeyBoardListener {
    override fun layoutId(): Int = R.layout.activity_chat_message
    private var mFriendId: String? = null
    private var mFriendName: String? = null
    private var mUserName: String? = null
    private var mUserAvatar: String? = null
    private lateinit var mAdapter: ChatMessageAdapter
    private val mChatMultipleItemList = mutableListOf<ChatMultipleItem>()
    //监听键盘发送事件
    private val onEmoticonClickListener = OnEmoticonClickListener<Emoticon> {
        when (it) {
            is DeleteEmoticon -> {
                val action = KeyEvent.ACTION_DOWN
                val code = KeyEvent.KEYCODE_DEL
                val event = KeyEvent(action, code)
                emoticonsBoard.etChat.onKeyDown(KeyEvent.KEYCODE_DEL, event)
            }
            is PlaceHoldEmoticon -> { // do nothing
            }
            is BigEmoticon -> {

            }
            else -> {
                val content: String? = it.code
                if (!content.isNullOrEmpty()) {
                    val index: Int = emoticonsBoard.etChat.selectionStart
                    val editable: Editable = emoticonsBoard.etChat.text
                    editable.insert(index, content)
                }
            }
        }
    }


    override fun initView(savedInstanceState: Bundle?) {
        mFriendId = intent.getStringExtra("friendId")
        mFriendName = intent.getStringExtra("friendName")
        mUserName = intent.getStringExtra("userName")
        mUserAvatar = intent.getStringExtra("userAvatar")
        LogUtils.i("mFriendId=$mFriendId")
        LogUtils.i("mFriendName=$mFriendName")
        tvFriendName.text = mFriendName
        initRecyclerView()
        initChatBoard()
        initLiveDataObserver()
        mViewModel.init(mUserName, mFriendName, mUserAvatar)
    }

    /**
     * 初始化环信的数据
     * 根据聊天的id加载会话信息
     */
    private fun initRecyclerView() {
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            mViewModel.loadMoreMessage()
        }
        mAdapter = ChatMessageAdapter(mChatMultipleItemList, mUserAvatar)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        recyclerView.adapter = mAdapter
    }

    /**
     * 初始化底部聊天板
     */
    private fun initChatBoard() {
        emoticonsBoard.etChat.addEmoticonFilter(EmojiFilter())
        val adapter = AdapterUtils.getCommonAdapter(this, onEmoticonClickListener)
        emoticonsBoard.setAdapter(adapter)
        emoticonsBoard.addOnFuncKeyBoardListener(this)
        emoticonsBoard.addFuncView(SimpleAppsGridView(this))
        emoticonsBoard.getEtChat()
            .setOnSizeChangedListener(OnSizeChangedListener { w, h, oldw, oldh -> scrollToBottom() })
        emoticonsBoard.btnSend.setOnClickListener {
            val content = emoticonsBoard.etChat.text.toString().trim()
            if (content.isNotEmpty() && mFriendId != null) {
                LogUtils.e("发送消息=$content")
                emoticonsBoard.etChat.setText("")
                mUserName?.let {
                    mViewModel.sendTextMessage(mFriendId!!, content, it)
                }
            }
        }
    }

    override fun onFuncClose() {
    }

    override fun onFuncPop(height: Int) {
        scrollToBottom()
    }

    /**
     * 注册监听事件
     */
    private fun initLiveDataObserver() {
        mViewModel.mInitConversationLiveData.observe(this, Observer {
            val list = it.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(list)
            mAdapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(0)
        })
        mViewModel.refreshResultLiveData.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
        })
        mViewModel.haveMoreDataLiveData.observe(this, Observer {
            val list = it.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(list)
            mAdapter.notifyDataSetChanged()
            recyclerView.post {
                recyclerView.smoothScrollBy(0, -200)
            }
        })
        //发送消息成功
        mViewModel.sendMessageSuccessLiveData.observe(this, Observer {
            val item = ChatMultipleItem(it)
            mChatMultipleItemList.add(0, item)
            mAdapter.notifyDataSetChanged()
        })
        //接收到消息
        mViewModel.receiveMessageLiveData.observe(this, Observer {
            val item = ChatMultipleItem(it)
            mChatMultipleItemList.add(0, item)
            mAdapter.notifyDataSetChanged()
        })
    }

    /**
     * 滚动到底部
     */
    private fun scrollToBottom() {
        recyclerView.post { recyclerView.smoothScrollToPosition(0) }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (EmoticonsKeyboardUtils.isFullScreen(this)) {
            if (emoticonsBoard.dispatchKeyEventInFullScreen(event)) {
                true
            } else {
                super.dispatchKeyEvent(event)
            }
        } else super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.addListener()
    }

    override fun onPause() {
        super.onPause()
        emoticonsBoard.reset()
        mViewModel.removeListener()
    }


}