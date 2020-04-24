package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ConversationModelWrap
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationViewModel : BaseViewModel() {
    val mConversationListLiveData = MutableLiveData<MutableList<ConversationModelWrap>>()
    val mSwipeRefreshLiveData = MutableLiveData<Boolean>()
    val deleteConversationResult = MutableLiveData<ConversationModelWrap>() //删除会话的回调
    val topConversationResult = MutableLiveData<Boolean>()//置顶会话的回调
    private val chatRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }

    /**
     * 加载会话列表
     */
    @Synchronized
    fun loadConversationList() {
        launchUI {
            try {
                val modelWrapList = mutableListOf<ConversationModelWrap>()
                withContext(Dispatchers.IO) {
                    if (BaseApplication.instance().mUserModel?.username == null) {
                        throw RuntimeException("当前未登陆")
                    }

                    val conversations =
                        EMClient.getInstance().chatManager().allConversations
                    conversations.values.forEach {
                        val userName = it.conversationId()
                        if (userName == SERVICE_USERNAME && BaseApplication.instance().mServiceModel != null) {
                            //客服的会话
                            val conversationEntity =
                                mConversationDao?.queryConversationByUserName(userName)
                            if (conversationEntity == null) {
                                val newConversationEntity = ConversationEntity(
                                    userName = userName,
                                    avatarUrl = BaseApplication.instance().mServiceModel!!.headUrl?:"",
                                    nickName = BaseApplication.instance().mServiceModel!!.nickName,
                                    sex = BaseApplication.instance().mServiceModel!!.sex.toString(),
                                    age = BaseApplication.instance().mServiceModel!!.age.toString(),
                                    isTop = conversationEntity?.isTop ?: false,
                                    lastMessageTime = it.lastMessage.msgTime,
                                    parentUserName = BaseApplication.instance().mUserModel!!.username
                                )
                                mConversationDao?.insert(newConversationEntity)
                            }
                        } else {
                            val userEntity = mUserDao?.queryUserByUserName(userName)
                            val conversationEntity =
                                mConversationDao?.queryConversationByUserName(userName)
                            if (userEntity != null) {
                                if (conversationEntity != null)
                                    mConversationDao?.delete(conversationEntity)
                                val newConversationEntity = ConversationEntity(
                                    userName = userName,
                                    avatarUrl = userEntity.avatarUrl,
                                    nickName = userEntity.nickName,
                                    remarkName = userEntity.remarkName,
                                    sex = userEntity.sex,
                                    age = userEntity.age,
                                    isTop = conversationEntity?.isTop ?: false,
                                    lastMessageTime = it.lastMessage.msgTime,
                                    parentUserName = conversationEntity?.parentUserName
                                        ?: BaseApplication.instance().mUserModel!!.username
                                )
                                mConversationDao?.insert(newConversationEntity)
                            }
                        }

                    }
                    val conversationEntityList =
                        mConversationDao?.queryConversationByParentUserName(
                            BaseApplication.instance().mUserModel!!.username
                        )
                    //置顶的会话
                    val topList = conversationEntityList?.filter {
                        it.isTop
                    }?.toMutableList()
                    val listBySort = conversationEntityList?.filter {
                        !it.isTop
                    }?.sortedBy {
                        it.lastMessageTime
                    }?.reversed()?.toMutableList()
                    val list = mutableListOf<ConversationEntity>()
                    topList?.let {
                        list.addAll(it)
                    }
                    listBySort?.let {
                        list.addAll(it)
                    }
                    list.forEach {
                        val userName = it.userName
                        val conversation = conversations.values.find { emConversation ->
                            emConversation.conversationId() == userName
                        }
                        modelWrapList.add(ConversationModelWrap(conversation, it))
                    }
                }
                mConversationListLiveData.postValue(modelWrapList)
                mSwipeRefreshLiveData.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                mSwipeRefreshLiveData.postValue(false)
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
                    if (item.conversationEntity != null) {
                        item.conversationEntity.isTop = !item.conversationEntity.isTop
                        mConversationDao?.update(item.conversationEntity)
                    }
                }
                topConversationResult.postValue(true)
            } catch (e: Throwable) {
                e.printStackTrace()
                topConversationResult.postValue(false)
            }
        }

    }

}