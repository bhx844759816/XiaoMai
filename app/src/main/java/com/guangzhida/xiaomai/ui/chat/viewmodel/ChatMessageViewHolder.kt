package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * 聊天界面的ViewHolder
 */
class ChatMessageViewHolder : BaseViewModel() {
    private val pagesize = 20
    private var mChatUserName: String? = null;
    private var mChatNickName: String? = null;
    private var mChatUserAvatar: String? = null;
    private val mRepository = InjectorUtil.getChatRepository()

    private lateinit var conversation: EMConversation
    val mInitConversationLiveData = MutableLiveData<List<EMMessage>>()
    val haveMoreDataLiveData = MutableLiveData<List<EMMessage>>() //是否有更多数据
    val refreshResultLiveData = MutableLiveData<Boolean>() //是否有更多数据
    val sendMessageSuccessLiveData = MutableLiveData<EMMessage>() //是否有更多数据
    val receiveMessageLiveData = MutableLiveData<EMMessage>() //是否有更多数据

    private val onEMMessageListener = object : EMMessageListener {
        override fun onMessageRecalled(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageChanged(message: EMMessage?, change: Any?) {
        }

        override fun onCmdMessageReceived(messages: MutableList<EMMessage>?) {

        }

        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
            messages?.let {
                _onMessageReceived(it)
            }
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {

        }
    }

    /**
     * 发送文本消息
     * @param friendId 好友ID
     * @param content 消息文本内容
     */
    fun sendTextMessage(friendId: String, content: String, userName: String) {
        launchGo(
            {
                //设置消息body
                mRepository.sendTextMsg(friendId, content)
                //插入一条消息到会话的尾部
                val txtBody =
                    EMMessage.createTxtSendMessage(content, userName)
                conversation.appendMessage(txtBody)
                sendMessageSuccessLiveData.postValue(txtBody)
            }, isShowDialog = false
        )
    }

    /**
     * 初始化EMConversation
     */
    fun init(userName: String?, nickName:String?,userAvatar: String?) {
        mChatUserName = userName
        mChatNickName = nickName
        mChatUserAvatar = userAvatar
        LogUtils.i("ChatMessageViewHolder init")
        conversation = EMClient.getInstance().chatManager()
            .getConversation(userName, EMConversation.EMConversationType.Chat, true)
        conversation.markAllMessagesAsRead()
        LogUtils.i(conversation.allMessages.toString())
        launchUI {
            val listResult = withContext(Dispatchers.IO) {
                EMClient.getInstance().chatManager().fetchHistoryMessages(
                    userName, EMConversation.EMConversationType.Chat, pagesize, ""
                )
                val msgCount = conversation.allMessages?.size ?: 0
                LogUtils.i("ChatMessageViewHolder msgCount=$msgCount")
                if (msgCount < conversation.allMsgCount && msgCount < pagesize) {
                    var msgId: String? = null
                    if (conversation.allMessages != null && conversation.allMessages.size > 0) {
                        msgId = conversation.allMessages[0].msgId
                    }
                    val list = conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount)
                    LogUtils.i("loadMoreMsgFromDB list=${list.size}")
                }
                conversation.allMessages
            }
            if (listResult != null) {
                mInitConversationLiveData.postValue(listResult)
            }
        }
    }

    /**
     * 拉取更多数据
     */
    fun loadMoreMessage() {
        launchUI {
            try {
                val listResult = withContext(Dispatchers.IO) {
                    val messages = conversation.loadMoreMsgFromDB(
                        if (conversation.allMessages.size == 0) "" else conversation.allMessages[0].msgId,
                        pagesize
                    )
                    messages
                }
                if (listResult != null && listResult.isNotEmpty()) {
                    LogUtils.i(listResult.toString())
                    haveMoreDataLiveData.postValue(listResult)
                }
                refreshResultLiveData.postValue(true)
            } catch (e1: Exception) {
                refreshResultLiveData.postValue(false)
            }
        }
    }

    /**
     * 注册监听事件
     */
    fun addListener() {
        EMClient.getInstance().chatManager().addMessageListener(onEMMessageListener)
    }

    /**
     * 移除监听事件
     */
    fun removeListener() {
        EMClient.getInstance().chatManager().removeMessageListener(onEMMessageListener)
    }

    /**
     * 接收到好友发来的消息
     */
    private fun _onMessageReceived(messages: List<EMMessage>) {
        for (message in messages) {
            var username: String? = null
            // group message
            username =
                if (message.chatType == EMMessage.ChatType.GroupChat || message.chatType == EMMessage.ChatType.ChatRoom) {
                    message.to
                } else { // single chat message
                    message.from
                }
            // if the message is for current conversation
            if (username == mChatUserName || message.to == mChatUserName || message.conversationId() == mChatUserName) {
                receiveMessageLiveData.postValue(message)
                conversation.markMessageAsRead(message.msgId)
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}