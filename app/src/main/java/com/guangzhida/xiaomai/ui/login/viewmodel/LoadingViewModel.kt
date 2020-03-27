package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.login.LoginNetwork
import com.guangzhida.xiaomai.data.login.LoginRepository
import com.guangzhida.xiaomai.utils.LogUtils

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
                loginFinish.postValue(true)
            },
            isShowDialog = false
        )
    }

}