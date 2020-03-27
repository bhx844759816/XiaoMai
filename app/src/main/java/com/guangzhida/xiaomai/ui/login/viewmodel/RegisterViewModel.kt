package com.guangzhida.xiaomai.ui.login.viewmodel

import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.login.LoginNetwork
import com.guangzhida.xiaomai.data.login.LoginRepository

/**
 * 注册的ViewModel
 */
class RegisterViewModel : BaseViewModel() {
    private val loginRepository = InjectorUtil.getLoginRepository()

    /**
     * 发送验证码
     */
    fun sendSmsCode(phone: String) {
        launchGo({
            val schoolModelWrap = loginRepository.sendSmsCode(phone, "1")
            if (schoolModelWrap.status == 200) {
                //获取验证码成功

            }
        })
    }

    /**
     * 注册
     */
    fun register(phone: String, smsCode: String, password: String) {
        launchGo(
            {
                val schoolModelWrap = loginRepository.register(phone, smsCode, password)
                if (schoolModelWrap.status == 200) {
                    //获取验证码成功

                }
            }
        )
    }

}