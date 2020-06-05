package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonInfoViewModel : BaseViewModel() {
    var mAddFriendResult = MutableLiveData<Boolean>()//添加好友的结果
    var mDeleteFriendResult = MutableLiveData<Boolean>()//删除好友的结果
    val mPersonInfoResult = MutableLiveData<Pair<Int, UserEntity>>()//获取用户信息
    private val mRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }

    /**
     * 添加好友
     *
     */
    fun addFriend(userEntity: UserEntity) {
        launchUI {
            try {
                val userModel = withContext(Dispatchers.IO) {
                    mUserDao?.queryUserByUserName(userEntity.userName)
                }
                if (userModel == null) {
                    withContext(Dispatchers.IO) {
                        EMClient.getInstance().contactManager()
                            .addContact(userEntity.userName, "请求加好友")
                        val inviteMessageEntity =
                            mInviteMessageDao?.queryInviteMessageByFrom(userEntity.userName)
                        if (inviteMessageEntity != null) {
                            mInviteMessageDao?.delete(inviteMessageEntity)
                        }
                        val insertInviteMessageEntity = InviteMessageEntity(
                            nickName = userEntity.nickName,
                            headerUrl = userEntity.avatarUrl,
                            from = userEntity.userName,
                            time = System.currentTimeMillis(),
                            reason = "请求加好友",
                            userName = BaseApplication.instance().mUserModel!!.username,
                            state = 1
                        )
                        mInviteMessageDao?.insert(insertInviteMessageEntity)
                    }
                    defUI.toastEvent.postValue("添加好友成功")
                    mAddFriendResult.postValue(true)
                } else {
                    defUI.toastEvent.postValue("已经是好友啦，不能重复添加")
                    mAddFriendResult.postValue(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                defUI.toastEvent.postValue("添加好友失败")
                mAddFriendResult.postValue(false)
            }

        }
    }

    /**
     * 删除好友
     */
    fun deleteFriend(userEntity: UserEntity) {
        launchGo({
            val result = mRepository.removeFriend(userEntity.uid.toString())
            if (result.isSuccess()) {
                //删除本地存储好友信息
                mUserDao?.let {
                    val localUserEntity = it.queryUserByUserName(userEntity.userName);
                    if (localUserEntity != null) {
                        it.delete(localUserEntity)
                    }
                }
                //删除本地好友邀请列表数据
                mInviteMessageDao?.let {
                    val inviteMessage = it.queryInviteMessageByFrom(userEntity.userName)
                    if (inviteMessage != null) {
                        it.delete(inviteMessage)
                    }
                }
                mConversationDao?.let {
                    val conversation = it.queryConversationByUserName(userEntity.userName)
                    if (conversation != null) {
                        it.delete(conversation)
                    }
                }
                //删除会话并清空消息
                EMClient.getInstance().chatManager().deleteConversation(userEntity.userName, true)
                defUI.toastEvent.postValue("删除好友成功")
                mDeleteFriendResult.postValue(true)
            } else {
                defUI.toastEvent.postValue(result.message)
                mDeleteFriendResult.postValue(false)
            }
        })
    }

    fun getUserInfoByUserName(userName: String) {
        launchUI {
            try {
                val userEntity = withContext(Dispatchers.IO) {
                    mUserDao?.queryUserByUserName(userName)
                }
                //本地有好友的缓存
                if (userEntity != null) {
                    mPersonInfoResult.postValue(Pair(0, userEntity))
                } else {
                    val result = withContext(Dispatchers.IO) {
                        mRepository.getUserInfoByNickNameOrPhone(userName)
                    }
                    if (result.isSuccess()) {
                        val chatUserModel = result.data[0]
                        val remoteUserEntity = UserEntity(
                            uid = chatUserModel.id.toLong(),
                            nickName = chatUserModel.nickName,
                            userName = chatUserModel.mobilePhone,
                            avatarUrl = chatUserModel.headUrl ?: "",
                            age = chatUserModel.age.toString(),
                            sex = chatUserModel.sex.toString(),
                            singUp = chatUserModel.signature ?: ""
                        )
                        mPersonInfoResult.postValue(Pair(1, remoteUserEntity))
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                defUI.toastEvent.postValue("加载出错请稍后再试")
            }
        }
    }


}