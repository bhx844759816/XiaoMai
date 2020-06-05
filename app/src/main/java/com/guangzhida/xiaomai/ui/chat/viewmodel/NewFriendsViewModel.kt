package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.room.PrimaryKey
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.model.NewFriendModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewFriendsViewModel : BaseViewModel() {
    val mNewFriendModeLiveData = MutableLiveData<List<NewFriendModel>>()
    val operateResultLiveData = MutableLiveData<Boolean>()
    private val chatRepository = InjectorUtil.getChatRepository()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }

    /**
     * 加载新朋友
     */
    fun loadNewFriends() {
        if (BaseApplication.instance().mUserModel == null || mInviteMessageDao == null || mUserDao == null) {
            return
        }
        launchUI {
            val list = getNewFriends()
            mNewFriendModeLiveData.postValue(list)
        }
    }

    private suspend fun getNewFriends(): List<NewFriendModel> {
        val list = mutableListOf<NewFriendModel>()
        return withContext(Dispatchers.IO) {
            val inviteMessageList =
                mInviteMessageDao?.queryInviteMessageByUserName(BaseApplication.instance().mUserModel!!.username)
            inviteMessageList?.forEach {
                val userEntity = mUserDao!!.queryUserByUserName(it.from)
                LogUtils.i("userEntity=$userEntity")
                if (userEntity == null) {
                    //当是刚收到好友请求的时候去服务器拉取好友请求的个人信息
                    if (it.state == 0 || it.state == 1) {
                        val result = chatRepository.getUserInfoByNickNameOrPhone(phone = it.from)
                        //只有初始状态的
                        if (result.isSuccess()) {
                            val data = result.data[0]
                            val model = NewFriendModel(
                                it, UserEntity(
                                    uid = data.id.toLong(),
                                    nickName = data.nickName,
                                    userName = data.mobilePhone,
                                    avatarUrl = data.headUrl ?: "",
                                    sex = data.sex.toString(),
                                    age = data.age.toString()
                                )
                            )
                            list.add(model)
                        }
                    }

                } else {
                    val model = NewFriendModel(it, userEntity)
                    list.add(model)
                }
            }
            list
        }

    }

    /**
     * 同意好友
     */
    fun agreeFriend(friendId: String, from: String) {
        launchGo({
            val result = chatRepository.agreeAddFriend(friendId, "1")
            if (result.isSuccess()) {
                val entity = mInviteMessageDao?.queryInviteMessageByFrom(from)
                if (entity != null) {
                    entity.state = 2
                }
                mInviteMessageDao?.update(entity)
                //将好友信息保存到本地
                val chatUserModelResult = chatRepository.getUserInfoByNickNameOrPhone(phone = from)
                if (chatUserModelResult.isSuccess()) {
                    val chatUserModel = chatUserModelResult.data[0]
                    val localEntity = UserEntity(
                        uid = chatUserModel.id.toLong(),
                        nickName = chatUserModel.nickName,
                        userName = chatUserModel.mobilePhone,
                        avatarUrl = chatUserModel.headUrl ?: "",
                        age = chatUserModel.age.toString(),
                        sex = chatUserModel.sex.toString(),
                        singUp = chatUserModel.signature ?: ""
                    )
                    mUserDao?.insert(localEntity)
                }
                operateResultLiveData.postValue(true)
                val list = getNewFriends()
                mNewFriendModeLiveData.postValue(list)
            } else {
                defUI.toastEvent.postValue(result.message)
                operateResultLiveData.postValue(false)
            }
        })
    }
//    /**
//     * 获取好友列表
//     */
//    private suspend fun getNewFriends(): MutableList<NewFriendModel> {
//        val list = mutableListOf<NewFriendModel>()
//        val inviteMessageList =
//            mInviteMessageDao!!.queryInviteMessageByUserName(BaseApplication.instance().mUserModel!!.username)
//        inviteMessageList.forEach {
//            val userEntity = mUserDao!!.queryUserByUserName(it.from)
//            if (userEntity == null) {
//                val result = chatRepository.getUserInfoByNickNameOrPhone(phone = it.from)
//                if (result.isSuccess()) {
//                    val data = result.result[0]
//                    val model = NewFriendModel(
//                        it, UserEntity(
//                            uid = data.id.toLong(),
//                            nickName = data.nickName,
//                            userName = data.mobilePhone,
//                            avatarUrl = data.headUrl,
//                            sex = data.sex.toString(),
//                            age = data.age.toString()
//                        )
//                    )
//                    list.add(model)
//                }
//            } else {
//                val model = NewFriendModel(it, userEntity)
//                list.add(model)
//            }
//        }
//        return list

//    }

//    /**
//     * 拒绝好友
//     */
//    fun refuseFriend(friendId: String, from: String) {
//        launchGo({
//            val result = chatRepository.agreeAddFriend(friendId, "0")
//            if (result.isSuccess()) {
//                val entity = mInviteMessageDao?.queryInviteMessageByFrom(from)
//                if (entity != null) {
//                    entity.state = -1
//                }
//                mInviteMessageDao?.update(entity)
//                operateResultLiveData.postValue(true)
//                val list = getNewFriends()
//                mNewFriendModeLiveData.postValue(list)
//            } else {
//                defUI.toastEvent.postValue(result.message)
//                operateResultLiveData.postValue(false)
//            }
//        })
//    }

}