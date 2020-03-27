package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.chat.ChatNetWork
import com.guangzhida.xiaomai.data.chat.ChatRepository
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.utils.ToastUtils

class AddFriendsViewModel : BaseViewModel() {
    val mSearchResultLiveData = MutableLiveData<List<ChatUserModel>>()//搜索好友列表
    val mAddFriendLiveData = MutableLiveData<Boolean>()//添加好友结果
    private val chatRepository = InjectorUtil.getChatRepository()
    /**
     * 搜索好友
     */
    fun doSearch(keyWord: String) {
        launchOnlyResult({
            chatRepository.getUserInfoByNickNameOrPhone(phone = keyWord)
        }, {
            mSearchResultLiveData.postValue(it)
        })

    }

    fun addFriend(friendId: String) {
        launchGo({
            val result = chatRepository.sendAddFriends(friendId, "")
            if (result.isSuccess()) {
                mAddFriendLiveData.postValue(true)
            } else {
                ToastUtils.toastShort(result.message)
            }
        }, {
            mAddFriendLiveData.postValue(false)
        })

    }
}