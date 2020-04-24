package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

/**
 * 个人信息
 */
class UserMessageModel : BaseViewModel() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    val modifyUserMessageLiveData = MutableLiveData<Boolean>() //修改用户信息结果
    private val userRepository = InjectorUtil.getUserRepository()

    /**
     * 提交修改信息
     */
    fun uploadMessage(file: File?, params: MutableMap<String, String>) {
        launchUI {
            try {
                defUI.showDialog.call()
                //上传用户头像
                if (file != null) {
                    val result = withContext(Dispatchers.IO) {
                        userRepository.uploadImg(file)
                    }
                    if (result.status == 200) {
                        val headId = result.message.split(":")[0]
                        val headUrl = result.message.split(":")[1]
                        params["headId"] = headId
                        params["headUrl"] = headUrl
                    }
                }
                //更新用户信息
                val result = withContext(Dispatchers.IO) {
                    userRepository.updateUserInfo(params)
                }
                if (result.status == 200 && BaseApplication.instance().mUserModel != null) {
                    //修改本地保存的用户信息
                    val entity = BaseApplication.instance().mUserModel!!
                    entity.age = result.data?.age ?: 0
                    entity.sex = result.data?.sex ?: 1
                    entity.headUrl = result.data?.headUrl ?: ""
                    entity.name = result.data?.nickName ?: ""
                    entity.signature = result.data?.signature?:""
                    mUserGson = Gson().toJson(entity)
                    userModelChangeLiveData.postValue(true)
                    modifyUserMessageLiveData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(result.message)
                    modifyUserMessageLiveData.postValue(false)
                }
                defUI.dismissDialog.call()
            } catch (e: Exception) {
                e.printStackTrace()
                defUI.dismissDialog.call()
                modifyUserMessageLiveData.postValue(false)
            }
        }
    }
}