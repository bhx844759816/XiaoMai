package com.guangzhida.xiaomai.ui

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : BaseViewModel() {
    val mFriendInviteCount = MutableLiveData<Int>()
    private val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }

    /**
     * 获取好友请求
     *
     * TODO 需要重构 改变收到好友请求的时候才取值
     */
    fun getFriendInvite() {
        launchUI {
            try {
                val count = withContext(Dispatchers.IO) {
                    val list =
                        mInviteMessageDao?.queryInviteMessageByUserName(BaseApplication.instance().mUserModel!!.username)
                    val filterList = list?.filter {
                        it.state == 0
                    }
                    LogUtils.i("好友请求=${filterList?.size}")
                    filterList?.size ?: 0
                }
                mFriendInviteCount.postValue(count)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}