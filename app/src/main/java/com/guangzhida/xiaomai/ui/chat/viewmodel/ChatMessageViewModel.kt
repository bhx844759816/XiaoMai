package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMCallBack
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
    private lateinit var mChatUserName: String
    private val mRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }

    var conversation: EMConversation? = null
    val mInitConversationLiveData = MutableLiveData<List<EMMessage>>()
    val mInitUserInfoObserver = MutableLiveData<Pair<String, String>>() //获取到好友的信息
    val mUserAvatarLiveData = MutableLiveData<String>()
    val haveMoreDataLiveData = MutableLiveData<List<EMMessage>>() //是否有更多数据
    val refreshResultLiveData = MutableLiveData<Boolean>() //下拉刷新回调
    val deleteFriendLiveData = MutableLiveData<Boolean>() //删除好友回调
    val isFriendLiveData = MutableLiveData<Boolean>() //删除好友回调
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

        }

        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
            messages?.let {
                innerOnMessageReceived(it)
            }
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {
            //消息已读
        }
    }

    /**
     * 内部进行统一处理消息发送的回调
     */
    private inner class InnerEmCallBack(private val emMessage: EMMessage) : EMCallBack {
        override fun onSuccess() {
            saveConversation()
            sendMessageSuccessLiveData.postValue(emMessage)
        }

        override fun onProgress(progress: Int, status: String?) {
        }

        override fun onError(code: Int, error: String?) {
            LogUtils.i("发送消息失败:$error")
            defUI.toastEvent.postValue("发送消息失败:$error")

        }
    }


    /**
     *
     * @param userName 聊天用户的ID
     * @param state 0为默认状态 1为从查询聊天记录进来的
     */
    fun init(userName: String, state: Int, loadFromMsgId: String = "", pageSize: Int = 0) {
        mChatUserName = userName
        launchUI {
            try {
                //
                withContext(Dispatchers.IO) {
                    val userEntity = mUserDao?.queryUserByUserName(userName)
                    isFriendLiveData.postValue(userEntity != null)
                    val conversationEntity =
                        mConversationDao?.queryConversationByUserName(userName)
                    if (conversationEntity != null) {
                        mInitUserInfoObserver.postValue(
                            Pair(
                                conversationEntity.nickName,
                                conversationEntity.avatarUrl
                            )
                        )
                    } else {
                        if (userEntity != null) {
                            mInitUserInfoObserver.postValue(
                                Pair(
                                    userEntity.nickName,
                                    userEntity.avatarUrl
                                )
                            )
                        }
                    }

                    //初始化Conversation
                    conversation = EMClient.getInstance().chatManager()
                        .getConversation(userName, EMConversation.EMConversationType.Chat, true)
                    conversation?.markAllMessagesAsRead()
                }
                //默认初始化
                if (state == 0) {
                    initMessage()
                } else if (state == 1) {
                    withContext(Dispatchers.IO) {
                        conversation?.loadMoreMsgFromDB(loadFromMsgId, pageSize)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private suspend fun initMessage() {
        val listResult = withContext(Dispatchers.IO) {
            val msgCount = conversation!!.allMessages?.size ?: 0
            if (msgCount < conversation!!.allMsgCount && msgCount < pageSize) {
                var msgId: String? = null
                if (conversation!!.allMessages != null && conversation!!.allMessages.size > 0) {
                    msgId = conversation!!.allMessages[0].msgId
                }
                conversation!!.loadMoreMsgFromDB(msgId, pageSize - msgCount)

            }
            conversation!!.allMessages
        }
        LogUtils.i("listResult=$listResult")
        if (listResult != null) {
            mInitConversationLiveData.postValue(listResult)
        }
    }


    /**
     * 发送文本消息
     * @param friendId 好友ID
     * @param content 消息文本内容
     */
    fun sendTextMessage(content: String) {
        LogUtils.i("sendTextMessage to $mChatUserName content=$content")
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    val txtBody = EMMessage.createTxtSendMessage(content, mChatUserName)
                    txtBody.setMessageStatusCallback(InnerEmCallBack(txtBody))
                    EMClient.getInstance().chatManager().sendMessage(txtBody)
                }

            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.toastEvent.postValue("发送消息失败")
            }
        }
    }

    /**
     * 发送语音
     */
    fun sendVoiceMessage(file: File, timeLen: Long) {
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    val voiceBody =
                        EMMessage.createVoiceSendMessage(
                            file.absolutePath,
                            timeLen.toInt(),
                            mChatUserName
                        )
                    voiceBody.setMessageStatusCallback(InnerEmCallBack(voiceBody))
                    EMClient.getInstance().chatManager().sendMessage(voiceBody)
                }
                saveConversation()
            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.toastEvent.postValue("发送消息失败")
            }
        }
    }

    /**
     * 发送图片消息
     */
    fun sendPicMessage(file: File) {
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    val picBody =
                        EMMessage.createImageSendMessage(file.absolutePath, false, mChatUserName)
                    picBody.setMessageStatusCallback(InnerEmCallBack(picBody))
                    EMClient.getInstance().chatManager().sendMessage(picBody)
                }
                saveConversation()
            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.toastEvent.postValue("发送图片消息失败")
            }
        }
    }

    /**
     * 添加好友
     */
    fun addFriend() {
        launchGo({
            val userEntity = mUserDao?.queryUserByUserName(mChatUserName)
            if (userEntity == null) {
                EMClient.getInstance().contactManager()
                    .addContact(mChatUserName, "请求加好友")
                val inviteMessageEntity =
                    mInviteMessageDao?.queryInviteMessageByFrom(mChatUserName)
                if (inviteMessageEntity != null) {
                    mInviteMessageDao?.delete(inviteMessageEntity)
                }
                val result = mRepository.getUserInfoByNickNameOrPhone(mChatUserName)
                if (result.isSuccess() && result.data.isNotEmpty()) {
                    val userModel = result.data[0]
                    val insertInviteMessageEntity = InviteMessageEntity(
                        nickName = userModel.nickName,
                        headerUrl = userModel.headUrl,
                        from = userModel.mobilePhone,
                        time = System.currentTimeMillis(),
                        reason = "请求加好友",
                        userName = BaseApplication.instance().mUserModel!!.username,
                        state = 1
                    )
                    mInviteMessageDao?.insert(insertInviteMessageEntity)
                    defUI.toastEvent.postValue("添加好友成功")
                } else {
                    defUI.toastEvent.postValue("查询不到用户信息")
                }
            } else {
                defUI.toastEvent.postValue("已经是好友啦，不能重复添加")
            }
        })
    }


    /**
     * 删除好友
     */
    fun deleteFriends() {
        launchGo({
            //查询本地的好友
            val userEntity = mUserDao?.queryUserByUserName(mChatUserName)
            val result = mRepository.removeFriend(userEntity!!.uid.toString())
            if (result.isSuccess()) {
                //删除本地存储好友信息
                mUserDao?.let {
                    val localUserEntity = it.queryUserByUserName(mChatUserName);
                    if (localUserEntity != null) {
                        it.delete(localUserEntity)
                    }
                }
                //删除本地好友邀请列表数据
                mInviteMessageDao?.let {
                    val inviteMessage = it.queryInviteMessageByFrom(mChatUserName)
                    if (inviteMessage != null) {
                        it.delete(inviteMessage)
                    }
                }
                //删除本地的会话
                mConversationDao?.let {
                    val conversation = it.queryConversationByUserName(mChatUserName)
                    if (conversation != null) {
                        it.delete(conversation)
                    }
                }
                //删除会话并清空消息
                EMClient.getInstance().chatManager().deleteConversation(mChatUserName, true)
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
        conversation?.removeMessage(message.msgId)
    }


    /**
     * 拉取更多数据
     */
    fun loadMoreMessage() {
        launchUI {
            try {
                if (conversation != null) {
                    val listResult = withContext(Dispatchers.IO) {
                        val messageList = conversation!!.allMessages
                        EMClient.getInstance().chatManager().fetchHistoryMessages(
                            mChatUserName, EMConversation.EMConversationType.Chat, pageSize,
                            if (messageList != null && messageList.size > 0) messageList[0].msgId else ""
                        )
                        val messages = conversation!!.loadMoreMsgFromDB(
                            if (conversation!!.allMessages.size == 0) "" else conversation!!.allMessages[0].msgId,
                            pageSize
                        )
                        messages
                    }
                    if (listResult != null && listResult.isNotEmpty()) {
                        LogUtils.i(listResult.toString())
                        haveMoreDataLiveData.postValue(listResult)
                    }
                    refreshResultLiveData.postValue(true)
                } else {
                    refreshResultLiveData.postValue(false)
                }
            } catch (e1: Exception) {
                refreshResultLiveData.postValue(false)
            }
        }
    }

    /**
     * 接收到好友发来的消息
     */
    private fun innerOnMessageReceived(messages: List<EMMessage>) {
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
                conversation?.markMessageAsRead(message.msgId)
            }
        }
    }

    /**
     * 添加监听
     */
    fun addListener() {
        EMClient.getInstance().chatManager().addMessageListener(onEMMessageListener)
    }

    /**
     * 移除监听
     */
    fun removeListener() {
        EMClient.getInstance().chatManager().removeMessageListener(onEMMessageListener)
    }

    override fun onCleared() {
        super.onCleared()
        conversation?.clear()
    }

    /**
     * 拉取最新的消息
     */
    fun pullNewMessage() {
        launchUI {
            try {
                val listResult = withContext(Dispatchers.IO) {
                    val messages = conversation!!.loadMoreMsgFromDB(
                        if (conversation!!.allMessages.size == 0) "" else conversation!!.allMessages[0].msgId,
                        Int.MAX_VALUE
                    )
                    conversation?.markAllMessagesAsRead()
                    messages
                }
                if (listResult != null && listResult.isNotEmpty()) {
                    LogUtils.i(listResult.toString())
                    haveMoreDataLiveData.postValue(listResult)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 保存当前的用户的会话
     */
    private fun saveConversation() {
        launchUI {
            withContext(Dispatchers.IO) {
                LogUtils.i("sendTextMessage to $mChatUserName  saveConversation")
                val conversationEntity =
                    mConversationDao?.queryConversationByUserName(mChatUserName)
                LogUtils.i("sendTextMessage to $mChatUserName  saveConversation")
                if (conversationEntity == null) {
                    LogUtils.i("sendTextMessage to $mChatUserName  执行个TASK任务")
                    //执行个TASK任务
                    ChatHelper.updateConversationEntity(mChatUserName)
                }
            }
        }
    }
}