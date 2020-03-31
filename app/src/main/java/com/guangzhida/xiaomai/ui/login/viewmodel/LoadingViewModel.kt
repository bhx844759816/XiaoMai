package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient

/**
 * 首页Loading页面
 */
class LoadingViewModel : BaseViewModel() {

    val loginFinish = MutableLiveData<Boolean>()

    private val loginRepository = InjectorUtil.getLoginRepository()
    /**
     * 登录
     */
    fun doLogin(phone: String, password: String) {
        LogUtils.i("调用登录手机号$phone,密码$password")
        launchGo(
            {
                val result = loginRepository.login(phone, password)
                if (result.status == 200) {
                    BaseApplication.instance().userModel = result.data
                }
            },
            complete = {
                doChatLogin(phone, password)
            },
            isShowDialog = false
        )
    }

    fun doChatLogin(phone: String, password: String) {
        if (EMClient.getInstance().isLoggedInBefore) {
            //加载全部会话
            EMClient.getInstance().chatManager().loadAllConversations()
            loginFinish.postValue(true)
        } else {
            EMClient.getInstance().login(phone, password, object : EMCallBack {
                override fun onSuccess() {
                    loginFinish.postValue(true)
                }

                override fun onProgress(progress: Int, status: String?) {
                }

                override fun onError(code: Int, error: String?) {
                    loginFinish.postValue(true)
                }

            })
        }
    }

}