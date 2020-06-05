package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * 联系人列表
 */
class ContactListViewModel : BaseViewModel() {
    val mContactListLiveData2 = MutableLiveData<List<UserEntity>>()
    val swipeRefreshResultLiveData = MutableLiveData<Boolean>()
    val mInviteMessageEntityListLiveData = MutableLiveData<MutableList<InviteMessageEntity>>()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    private val chatRepository = InjectorUtil.getChatRepository()

    /**
     *
     * 除了客服
     * 本人
     *
     * 其他的都删除掉
     *
     * 获取联系人列表
     */
    fun getContactList() {
        launchUI {
            try {
                //将后台好友关系拉取到本地
                val list = withContext(Dispatchers.IO) {
                    val result = chatRepository.getFriendList()
                    val userEntityList = mUserDao?.queryAll()
                    if (userEntityList != null && userEntityList.isNotEmpty()) {
                        mContactListLiveData2.postValue(userEntityList)
                    }
                    if (result.isSuccess()) {
                        //遍历服务器中所有的好友信息将其更新到本地
                        val list = result.data.map { chatUserModel ->
                            val localUserEntity = userEntityList?.find {
                                it.uid == chatUserModel.id.toLong()
                            }
                            UserEntity(
                                uid = chatUserModel.id.toLong(),
                                nickName = chatUserModel.nickName,
                                userName = chatUserModel.mobilePhone,
                                avatarUrl = chatUserModel.headUrl?:"",
                                age = chatUserModel.age.toString(),
                                sex = chatUserModel.sex.toString(),
                                singUp = chatUserModel.signature?: "",
                                remarkName = localUserEntity?.remarkName ?: ""
                            )
                        }
                        //删除服务器未存在的本地好友信息
                        userEntityList?.forEach {
                            mUserDao?.delete(it)
                        }
                        list.forEach {
                            mUserDao?.insert(it)
                        }
                    }
                    mUserDao?.queryAll()
                }
                if (list != null && list.isNotEmpty()) {
                    mContactListLiveData2.postValue(list)
                }
                //获取好友请求列表
                val inviteMessageList = loadInviteMessageList()
                if (inviteMessageList != null) {
                    mInviteMessageEntityListLiveData.postValue(inviteMessageList.toMutableList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }finally {
                swipeRefreshResultLiveData.postValue(true)
            }
        }
    }

    /**
     * 加载好友列表
     */
    private suspend fun loadInviteMessageList(): List<InviteMessageEntity>? {
        return withContext(Dispatchers.IO) {
            val list =
                mInviteMessageDao?.queryInviteMessageByUserName(BaseApplication.instance().mUserModel!!.username)
            //更新头像和昵称显示
            list?.forEach {
                if (it.nickName == null) {
                    val result = chatRepository.getUserInfoByNickNameOrPhone(phone = it.from)
                    if (result.isSuccess()) {
                        val chatUserModel = result.data[0]
                        it.nickName = chatUserModel.nickName
                        it.headerUrl = chatUserModel.headUrl
                        mInviteMessageDao?.update(it)
                    }
                }
            }
            // 只有刚接受的状态才显示
            list?.filter {
                it.state == 0
            }
        }
    }

}