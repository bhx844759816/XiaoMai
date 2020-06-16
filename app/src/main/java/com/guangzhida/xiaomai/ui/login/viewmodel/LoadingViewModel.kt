package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * 首页Loading页面
 */
class LoadingViewModel : BaseViewModel() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    val loadingFinish = MutableLiveData<Boolean>()
    private val loginRepository = InjectorUtil.getLoginRepository()


    /**
     * 验证token
     */
    fun verifyToken() {
        launchUI {
            try {
                if (BaseApplication.instance().mUserModel == null) {
                    withContext(Dispatchers.IO) {
                        delay(3000)
                        loadingFinish.postValue(true)
                    }
                } else {
                    val result = withContext(Dispatchers.IO) {
                        loginRepository.verifyToken(BaseApplication.instance().mUserModel!!.token)
                    }
                    if (result.isSuccess()) {
                        loadingFinish()
                    } else {
                        val refreshTokenResult = withContext(Dispatchers.IO) {
                            loginRepository.refreshToken()
                        }
                        if (refreshTokenResult.isSuccess()) {
                            BaseApplication.instance().mUserModel?.token = refreshTokenResult.data
                            mUserGson = Gson().toJson(BaseApplication.instance().mUserModel)
                        } else {
                            mUserGson = ""
                            BaseApplication.instance().mUserModel = null
                        }
                        loadingFinish()
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                loadingFinish.postValue(true)
            }
        }
    }

    /**
     * 加载完成
     */
    private suspend fun loadingFinish() {
        withContext(Dispatchers.IO) {
            EMClient.getInstance().chatManager().loadAllConversations()
            loadingFinish.postValue(true)
        }
    }

}