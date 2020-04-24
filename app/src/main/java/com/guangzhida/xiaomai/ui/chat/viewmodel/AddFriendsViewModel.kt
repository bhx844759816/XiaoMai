package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.chat.ChatNetWork
import com.guangzhida.xiaomai.data.chat.ChatRepository
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class AddFriendsViewModel : BaseViewModel() {
    val mSearchResultLiveData = MutableLiveData<List<ChatUserModel>>()//搜索好友列表
    val mAddFriendLiveData = MutableLiveData<Boolean>()//添加好友结果
    private val chatRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }


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

    fun addFriend(chatUserModel: ChatUserModel) {
        launchUI {
            val userModel = withContext(Dispatchers.IO) {
                mUserDao?.queryUserByUserName(chatUserModel.mobilePhone)
            }
            if (userModel == null) {
                try {
                    withContext(Dispatchers.IO) {
                        EMClient.getInstance().contactManager()
                            .addContact(chatUserModel.mobilePhone, "请求加好友")
                        val inviteMessageEntity =
                            mInviteMessageDao?.queryInviteMessageByFrom(chatUserModel.mobilePhone)
                        if (inviteMessageEntity != null) {
                            mInviteMessageDao?.delete(inviteMessageEntity)
                        }
                        val insertInviteMessageEntity = InviteMessageEntity(
                            nickName = chatUserModel.nickName,
                            headerUrl = chatUserModel.headUrl,
                            from = chatUserModel.mobilePhone,
                            time = System.currentTimeMillis(),
                            reason = "请求加好友",
                            userName = BaseApplication.instance().mUserModel!!.username,
                            state = 1
                        )
                        mInviteMessageDao?.insert(insertInviteMessageEntity)
                    }
                    mAddFriendLiveData.postValue(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mAddFriendLiveData.postValue(false)
                }
            } else {
                defUI.toastEvent.postValue("已经是好友啦，不能重复添加")
            }
        }
//      launchGo({
//            val result = chatRepository.sendAddFriends(friendId, "")
//            if (result.isSuccess()) {
//                mAddFriendLiveData.postValue(true)
//            } else {
//                ToastUtils.toastShort(result.message)
//            }
//       }, {
//            mAddFriendLiveData.postValue(false)
//       })
    }
}