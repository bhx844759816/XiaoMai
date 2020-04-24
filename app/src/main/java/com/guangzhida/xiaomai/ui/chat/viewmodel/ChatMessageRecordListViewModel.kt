package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.model.ChatMessageRecordModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

/**
 * 聊天记录列表
 */
class ChatMessageRecordListViewModel : BaseViewModel() {
    val mChatMessageRecordModelList = MutableLiveData<List<ChatMessageRecordModel>>()
    val mQueryChatMessageRecord = MutableLiveData<List<EMMessage>>() //查询到的全部的聊天记录
    var mContentClickCallBack: ((UserEntity) -> Unit)? = null
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }

    /**
     * 模糊查询本地的和好友的聊天记录
     */
    fun queryChatMessageRecord(key: String, userName: String) {
        launchUI {
            try {
                val chatMessageRecordList = arrayListOf<ChatMessageRecordModel>()
                withContext(Dispatchers.IO) {
                    val conversation: EMConversation =
                        EMClient.getInstance().chatManager().getConversation(userName)
                            ?: throw RuntimeException("with $userName  conversation is null")

                    val list = conversation.searchMsgFromDB(
                        key,
                        0,
                        Int.MAX_VALUE,
                        "",
                        EMConversation.EMSearchDirection.DOWN
                    )
                    list.forEach { emMessage ->
                        val from = emMessage.from
                        val userEntity =
                            if (from == BaseApplication.instance().mUserModel?.username) {
                                //自己
                                UserEntity(
                                    uid = BaseApplication.instance().mUserModel?.id?.toLong() ?: 0,
                                    nickName = BaseApplication.instance().mUserModel?.name ?: "",
                                    userName = BaseApplication.instance().mUserModel?.username
                                        ?: "",
                                    avatarUrl = BaseApplication.instance().mUserModel?.headUrl
                                        ?: "",
                                    age = BaseApplication.instance().mUserModel?.age?.toString()
                                        ?: "",
                                    sex = BaseApplication.instance().mUserModel?.sex?.toString()
                                        ?: ""
                                )
                            } else {
                                //好友
                                mUserDao?.queryUserByUserName(userName)
                            }
                        if (emMessage.type == EMMessage.Type.TXT) {
                            val messageBody = emMessage.body as EMTextMessageBody
                            userEntity?.let {
                                val chatMessageRecordModel = ChatMessageRecordModel(
                                    messageId = emMessage.msgId,
                                    message = messageBody.message,
                                    atTime = emMessage.msgTime,
                                    userEntity = it
                                )
                                chatMessageRecordList.add(chatMessageRecordModel)
                            }
                        }

                    }

                }
                mChatMessageRecordModelList.postValue(chatMessageRecordList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 通过聊天的ID往下查询所有的聊天记录
     */
    fun queryChatMessageByMsgId(startTimeStamp: Long, userName: String) {
        launchUI {
            try {
                val list = withContext(Dispatchers.IO) {
                    val conversation: EMConversation =
                        EMClient.getInstance().chatManager().getConversation(userName)
                            ?: throw RuntimeException("with $userName  conversation is null")
                    conversation.searchMsgFromDB(
                        startTimeStamp - 1,
                        System.currentTimeMillis(),
                        Int.MAX_VALUE
                    )
                }
                mQueryChatMessageRecord.postValue(list)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }
}