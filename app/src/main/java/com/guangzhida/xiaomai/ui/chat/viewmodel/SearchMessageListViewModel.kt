package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.model.SearchMessageModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 搜索聊天记录列表
 */
class SearchMessageListViewModel : BaseViewModel() {
    val searchChatMessageMap = MutableLiveData<Map<String, SearchMessageModel>>()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }


    fun doSearchChatMessage(key: String){
        launchUI {
            //检索聊天记录
            val messageMap = withContext(Dispatchers.IO) {
                val messageMap = mutableMapOf<String, SearchMessageModel>()
                //对象(头像 昵称 聊天记录条数)
                val list = EMClient.getInstance().chatManager()
                    .searchMsgFromDB(key, 0, 100, "", EMConversation.EMSearchDirection.DOWN)
                list.forEach {
                    val from = it.from
                    val to = it.to
                    val userName = if (from != BaseApplication.instance().mUserModel!!.username) {
                        from
                    } else {
                        to
                    }
                    if (messageMap.containsKey(userName)) {
                        val searchMessageModel = messageMap[userName]
                        if (searchMessageModel != null) {
                            val count = searchMessageModel.messageCount
                            searchMessageModel.messageCount = count + 1
                        }
                    } else {
                        val userEntity = mUserDao?.queryUserByUserName(userName)
                        if (userEntity != null) {
                            val searchMessageModel = SearchMessageModel(
                                userEntity = userEntity,
                                messageCount = 1
                            )
                            messageMap[userName] = searchMessageModel
                        }
                    }
                }
                messageMap
            }
            LogUtils.i("检索聊天记录=$messageMap")
            searchChatMessageMap.postValue(messageMap)
        }
    }
}