package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 联系人列表
 */
class ContactListViewModel : BaseViewModel() {
    val mContactListLiveData = MutableLiveData<List<ChatUserModel>>()//搜索好友列表
    private val chatRepository = InjectorUtil.getChatRepository()

    /**
     * 获取联系人列表
     */
    fun getContactList() {
        launchGo({
            val result = chatRepository.getFriendList()
            if (result.isSuccess()) {
                mContactListLiveData.postValue(result.result)
            }
        }, isShowDialog = false)
    }


}