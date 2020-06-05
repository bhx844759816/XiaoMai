package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.model.SearchMessageModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 搜索的ViewModel
 */
class SearchViewModel : BaseViewModel() {
    val searchUserEntityList = MutableLiveData<List<UserEntity>>()
    val searchChatMessageMap = MutableLiveData<Map<String, SearchMessageModel>>()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }

    /**
     * 检索 好友 和聊天记录
     */
    fun doSearch(key: String) {
        LogUtils.i("key=$key")
        launchUI {
            //检索好友
            val userEntityList = withContext(Dispatchers.IO) {
                LogUtils.i("%$key%")
                mUserDao?.queryUserByLike(key)
            }
            if (userEntityList != null) {
                LogUtils.i("检索好友=$userEntityList")
                searchUserEntityList.postValue(userEntityList)
            }
            //检索聊天记录
            val messageMap = withContext(Dispatchers.IO) {
                val messageMap = mutableMapOf<String, SearchMessageModel>()
                //对象(头像 昵称 聊天记录条数)
                val list = EMClient.getInstance().chatManager()
                    .searchMsgFromDB(key, 0, Int.MAX_VALUE, "", EMConversation.EMSearchDirection.DOWN)
                LogUtils.i("doSearch list=$list")
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
                        //查询好友
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