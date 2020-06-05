package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
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
open class ChatServiceMessageViewModel : BaseViewModel() {
    private val pageSize = 20
    private lateinit var mChatUserName: String
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }

    var conversation: EMConversation? = null
    var mServiceModel: ServiceModel? = null
    val mInitConversationLiveData = MutableLiveData<List<EMMessage>>()
    val mInitUserInfoObserver = MutableLiveData<Pair<String, String>>() //获取到好友的信息
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
    fun init(serviceModel: ServiceModel) {
        if (serviceModel.username.isNullOrEmpty()) {
            ToastUtils.toastShort("客服id为空，初始化失败")
            return
        }
        mServiceModel = serviceModel
        mChatUserName = serviceModel.username
        LogUtils.i("chatUserName=$mChatUserName")
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    //初始化Conversation
                    conversation = EMClient.getInstance().chatManager()
                        .getConversation(
                            mChatUserName,
                            EMConversation.EMConversationType.Chat,
                            true
                        )
                    conversation?.markAllMessagesAsRead()
                }
                initMessage()
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
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    val txtBody = EMMessage.createTxtSendMessage(content, mChatUserName)
                    txtBody.setMessageStatusCallback(InnerEmCallBack(txtBody))
                    EMClient.getInstance().chatManager().sendMessage(txtBody)
                }
            } catch (e: Throwable) {
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
            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.toastEvent.postValue("发送图片消息失败")
            }
        }
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
                val conversationEntity =
                    mConversationDao?.queryConversationByUserName(mChatUserName)
                LogUtils.i("conversationEntity=$conversationEntity")
                if (conversationEntity == null && mServiceModel != null) {
                    mConversationDao?.insert(getConversationEntity(mServiceModel!!))
                } else {
                    conversationEntity?.nickName = mServiceModel?.nickName ?: ""
                    conversationEntity?.avatarUrl = mServiceModel?.headUrl ?: ""
                    conversationEntity?.sex = mServiceModel?.sex.toString()
                    conversationEntity?.age = mServiceModel?.age.toString()
                    conversationEntity?.avatarUrl = mServiceModel?.headUrl ?: ""
                    LogUtils.i("conversationEntity update=$conversationEntity")
                    mConversationDao?.update(conversationEntity)
                }
            }
        }
    }

    /**
     * 通过本地保存的用户对象UserEntity获取会话的Entity
     */
    private fun getConversationEntity(serviceModel: ServiceModel): ConversationEntity {
        return ConversationEntity(
            userName = serviceModel.username ?: "",
            avatarUrl = serviceModel.headUrl ?: "",
            nickName = serviceModel.nickName ?: "",
            sex = serviceModel.sex.toString(),
            age = serviceModel.age.toString(),
            type = 1,
            parentUserName = BaseApplication.instance().mUserModel?.username ?: ""
        )
    }
}