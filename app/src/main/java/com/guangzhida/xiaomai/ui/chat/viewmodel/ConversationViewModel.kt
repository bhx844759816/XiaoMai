package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication

import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ConversationModelWrap
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationViewModel : BaseViewModel() {
    val mConversationListLiveData = MutableLiveData<MutableList<ConversationModelWrap>>()
    val mSwipeRefreshLiveData = MutableLiveData<Boolean>()
    val deleteConversationResult = MutableLiveData<ConversationModelWrap>() //删除会话的回调
    val topConversationResult = MutableLiveData<Boolean>()//置顶会话的回调
    val mConversationTypeObserver = MutableLiveData<Pair<Int,String>>()
    private val chatRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }


    /**
     * 加载本地的会话列表
     */
    fun loadConversation() {
        launchUI {
            try {
                val modelWrapList = mutableListOf<ConversationModelWrap>()
                withContext(Dispatchers.IO) {
                    val _modelList = mutableListOf<ConversationModelWrap>()
                    val conversations =
                        EMClient.getInstance().chatManager().allConversations
                    LogUtils.i("conversations=$conversations")
                    conversations.forEach {
                        val userName = it.value.conversationId()
                        val conversationEntity =
                            mConversationDao?.queryConversationByUserName(userName)
                        if (conversationEntity != null) {
                            conversationEntity.lastMessageTime =
                                if (it.value.lastMessage != null) it.value.lastMessage.msgTime else 0
                            val conversationModelWrap = ConversationModelWrap(
                                emConversation = it.value,
                                conversationEntity = conversationEntity
                            )
                            _modelList.add(conversationModelWrap)
                        }
                    }
                    val topList = _modelList.filter {
                        it.conversationEntity.isTop
                    }.toMutableList()
                    val listBySort = _modelList.filter {
                        !it.conversationEntity.isTop
                    }.sortedBy {
                        it.conversationEntity.lastMessageTime
                    }.reversed().toMutableList()
                    modelWrapList.addAll(topList)
                    modelWrapList.addAll(listBySort)
                }
                mConversationListLiveData.postValue(modelWrapList)
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                mSwipeRefreshLiveData.postValue(true)
            }
        }
    }


    /**
     * 删除会话
     */
    fun deleteConversation(wrap: ConversationModelWrap) {
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    val result =
                        EMClient.getInstance().chatManager()
                            .deleteConversation(wrap.emConversation?.conversationId(), true)
                    if (result) {
                        val conversationEntity =
                            mConversationDao?.queryConversationByUserName(
                                wrap.emConversation?.conversationId() ?: ""
                            )
                        if (conversationEntity != null) {
                            mConversationDao?.delete(conversationEntity)
                        }
                        deleteConversationResult.postValue(wrap)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                deleteConversationResult.postValue(null)
            }

        }

    }

    /**
     * 置顶会话
     */
    fun makeConversationTop(item: ConversationModelWrap) {
        launchUI {
            try {
                withContext(Dispatchers.IO) {
                    item.conversationEntity.isTop = !item.conversationEntity.isTop
                    mConversationDao?.update(item.conversationEntity)
                }
                topConversationResult.postValue(true)
            } catch (e: Throwable) {
                e.printStackTrace()
                topConversationResult.postValue(false)
            }
        }
    }

}