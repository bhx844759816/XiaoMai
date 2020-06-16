package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ChatUserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 全部已报名的ViewModel
 */
class AllSingUpUserViewModel : BaseViewModel() {
    private val mChatRepository = InjectorUtil.getChatRepository()

    val mChatUserModelObserver = MutableLiveData<List<ChatUserModel>>()

    /**
     * 获取已报名的用户列表
     */
    fun getSignUpUserList(activityId: String) {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    mChatRepository.getSignUpUserByActivityId(activityId)
                }
                if (result.isSuccess()) {
                    mChatUserModelObserver.postValue(result.data)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}