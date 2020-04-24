package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * 设置备注
 */
class SettingRemarkViewModel : BaseViewModel() {
    val modifyRemarkResult = MutableLiveData<Boolean>()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }


    fun modifyRemark(userEntity: UserEntity) {
        launchUI {
            try {
                defUI.showDialog.call()
                withContext(Dispatchers.IO) {
                    val localUserEntity =
                        mUserDao?.queryUserByUserName(userName = userEntity.userName)
                    if (localUserEntity == null) {
                        mUserDao?.insert(userEntity)
                    } else {
                        mUserDao?.update(userEntity)
                    }
                }
                modifyRemarkResult.postValue(true)
                defUI.dismissDialog.call()
            } catch (e: Exception) {
                modifyRemarkResult.postValue(false)
                defUI.dismissDialog.call()
            }

        }
    }
}