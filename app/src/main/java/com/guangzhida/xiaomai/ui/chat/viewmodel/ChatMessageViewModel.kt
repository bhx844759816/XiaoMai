package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.EaseUiHelper
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


/**
 * 聊天界面的ViewHolder
 */
open class ChatMessageViewModel : BaseViewModel() {
    private val pageSize = 20
    private var mChatUserName: String? = null;
    private val mRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    private lateinit var conversation: EMConversation
    val mInitConversationLiveData = MutableLiveData<List<EMMessage>>()
    val mUserAvatarLiveData = MutableLiveData<String>()
    val haveMoreDataLiveData = MutableLiveData<List<EMMessage>>() //是否有更多数据
    val refreshResultLiveData = MutableLiveData<Boolean>() //下拉刷新回调
    val deleteFriendLiveData = MutableLiveData<Boolean>() //删除好友回调
    val sendMessageSuccessLiveData = MutableLiveData<EMMessage>() //发送消息成功
    val receiveMessageLiveData = MutableLiveData<EMMessage>() //接收到消息

    private val onEMMessageListener = object : EMMessageListener {
        override fun onMessageRecalled(messages: MutableList<EMMessage>?) {
            //测回消息
        }

        override fun onMessageChanged(message: EMMessage?, change: Any?) {
            //消息改变
        }

        override fun onCmdMessageReceived(messages: MutableList<EMMessage>?) {
            //接收到透传消息
        }

        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
            LogUtils.i("onMessageReceived=${messages}")
            messages?.let {
                _onMessageReceived(it)
            }
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {
            //消息已读
        }
    }

    init {

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
                val result = mRepository.sendTextMsg(friendId, content)
                if (result.status == 200) {
                    //插入一条消息到会话的尾部
                    val txtBody =
                        EMMessage.createTxtSendMessage(content, userName)
                    conversation.appendMessage(txtBody)
                    sendMessageSuccessLiveData.postValue(txtBody)
                }
            }, isShowDialog = false
        )
    }

    /**
     * 发送语音
     */
    fun sendVoiceMessage(
        friendId: String,
        file: File,
        timeLen: Long,
        userName: String
    ) {
        launchGo({
            val voiceBody =
                EMMessage.createVoiceSendMessage(file.absolutePath, timeLen.toInt(), userName)
            EMClient.getInstance().chatManager().sendMessage(voiceBody)
            sendMessageSuccessLiveData.postValue(voiceBody)
        }, isShowDialog = false)
    }

    /**
     * 发送图片消息
     */
    fun sendPicMessage(
        friendId: String,
        file: File, userName: String
    ) {
        launchGo({
            val result = mRepository.sendPicOrVoiceMsg(friendId, "img", file)
            if (result.isSuccess()) {
                val picBody = EMMessage.createImageSendMessage(file.absolutePath, false, userName)
                conversation.appendMessage(picBody)
                sendMessageSuccessLiveData.postValue(picBody)
            }
        }, isShowDialog = false)
    }

    /**
     * 删除好友
     */
    fun deleteFriends(friendId: String, userName: String) {
        launchGo({
            val result = mRepository.removeFriend(friendId)
            if (result.isSuccess()) {
                //删除本地存储好友信息
                mUserDao?.let {
                    val userEntity = it.queryUserByUserName(userName);
                    it.delete(userEntity)
                }
                //删除本地好友邀请列表数据
                mInviteMessageDao?.let {
                    val inviteMessage = it.queryInviteMessageByFrom(userName)
                    if (inviteMessage != null) {
                        it.delete(inviteMessage)
                    }

                }
                //清空好友信息
                conversation.clearAllMessages()
                defUI.toastEvent.postValue("删除好友成功")
                deleteFriendLiveData.postValue(true)
            } else {
                defUI.toastEvent.postValue(result.message)
                deleteFriendLiveData.postValue(false)
            }
        })
    }

    /**
     * 删除单条聊天记录
     */
    fun deleteChatMessage(message: EMMessage) {
        conversation.removeMessage(message.msgId)
    }

    /**
     * 加载消息
     */
    fun initLocalMessage() {
        launchUI {
            try {
                val listResult = withContext(Dispatchers.IO) {
                    val msgCount = conversation.allMessages?.size ?: 0
                    if (msgCount < conversation.allMsgCount && msgCount < pageSize) {
                        var msgId: String? = null
                        if (conversation.allMessages != null && conversation.allMessages.size > 0) {
                            msgId = conversation.allMessages[0].msgId
                        }
                        val list = conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount)
                        LogUtils.i("loadMoreMsgFromDB list=$list")
                    }
                    conversation.allMessages
                }
                if (listResult != null) {
                    mInitConversationLiveData.postValue(listResult)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 加载此消息id后面的全部消息
     */
    fun initLocalMessage(msgId: String,pageSize:Int) {
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                   val list =  conversation.loadMoreMsgFromDB(msgId,pageSize)
                    LogUtils.i("list=$list")
                }
            } catch (e: Throwable) {

            }

        }
    }

    /**
     * 初始化会话对象
     */
    fun init(userName: String) {
        mChatUserName = userName
        conversation = EMClient.getInstance().chatManager()
            .getConversation(userName, EMConversation.EMConversationType.Chat, true)
        conversation.markAllMessagesAsRead()
    }

//    /**
//     * 初始化EMConversation
//     */
//    fun init(userName: String?) {
//        mChatUserName = userName
//        conversation = EMClient.getInstance().chatManager()
//            .getConversation(userName, EMConversation.EMConversationType.Chat, true)
//        conversation.markAllMessagesAsRead()
//        launchUI {
//            val listResult = withContext(Dispatchers.IO) {
//                //                try {
////                    //拉取漫游消息 - 需要开通此功能才能拉取
////                    EMClient.getInstance().chatManager().fetchHistoryMessages(
////                        userName, EMConversation.EMConversationType.Chat, pageSize, ""
////                    )
////                }catch (e:Exception){
////                    e.printStackTrace()
////                }
//                val msgCount = conversation.allMessages?.size ?: 0
//                if (msgCount < conversation.allMsgCount && msgCount < pageSize) {
//                    var msgId: String? = null
//                    if (conversation.allMessages != null && conversation.allMessages.size > 0) {
//                        msgId = conversation.allMessages[0].msgId
//                    }
//                    val list = conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount)
//                    LogUtils.i("loadMoreMsgFromDB list=${list.size}")
//                }
//                conversation.allMessages
//            }
//            if (listResult != null) {
//                mInitConversationLiveData.postValue(listResult)
//            }
//            val url = withContext(Dispatchers.IO) {
//                mUserDao?.queryUserByUserName(conversation.conversationId())?.avatarUrl
//            }
//            mUserAvatarLiveData.postValue(url)
//        }
//    }

    /**
     * 拉取更多数据
     */
    fun loadMoreMessage() {

        launchUI {
            try {
                val listResult = withContext(Dispatchers.IO) {
                    val messageList = conversation.allMessages
                    EMClient.getInstance().chatManager().fetchHistoryMessages(
                        mChatUserName, EMConversation.EMConversationType.Chat, pageSize,
                        if (messageList != null && messageList.size > 0) messageList[0].msgId else ""
                    )
                    val messages = conversation.loadMoreMsgFromDB(
                        if (conversation.allMessages.size == 0) "" else conversation.allMessages[0].msgId,
                        pageSize
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
     * 接收到好友发来的消息
     */
    private fun _onMessageReceived(messages: List<EMMessage>) {
        for (message in messages) {
            var username: String? = null
            username =
                if (message.chatType == EMMessage.ChatType.GroupChat || message.chatType == EMMessage.ChatType.ChatRoom) {
                    message.to
                } else {
                    message.from
                }
            if (username == mChatUserName || message.to == mChatUserName || message.conversationId() == mChatUserName) {
                receiveMessageLiveData.postValue(message)
                conversation.markMessageAsRead(message.msgId)
            }
            EaseUiHelper.notifier?.vibrateAndPlayTone()
        }
    }

    fun addListener() {
        EMClient.getInstance().chatManager().addMessageListener(onEMMessageListener)
    }

    fun removeListener() {
        EMClient.getInstance().chatManager().removeMessageListener(onEMMessageListener)
    }

    override fun onCleared() {
        super.onCleared()

        conversation.clear()
    }
}