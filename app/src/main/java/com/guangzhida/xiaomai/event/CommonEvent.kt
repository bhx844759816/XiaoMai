package com.guangzhida.xiaomai.event

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.receiver.WifiStateManager

/**
 * 网络状态切换到LiveData
 */
val netChangeLiveData = MutableLiveData<Boolean>()

/**
 * 用户信息改变的LiveData
 */
val userModelChangeLiveData = MutableLiveData<Boolean>()
/**
 * 学校信息改变的观察者
 */
val schoolModelChangeLiveData = MutableLiveData<SchoolModel>()
/**
 * 消息个数改变的观察者
 */
val messageCountChangeLiveData = MutableLiveData<Boolean>()
/**
 * 添加朋友
 */
val addFriendChangeLiveData = MutableLiveData<Boolean>()
