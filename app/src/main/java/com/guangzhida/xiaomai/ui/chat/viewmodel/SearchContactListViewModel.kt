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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchContactListViewModel : BaseViewModel() {
    val searchUserEntityList = MutableLiveData<List<UserEntity>>()

    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }

    /**
     * 检索 好友 和聊天记录
     */
    fun doSearchContactList(key: String) {
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
        }
    }

}