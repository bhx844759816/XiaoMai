package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.login.LoginNetwork
import com.guangzhida.xiaomai.data.login.LoginRepository

/**
 * 注册的ViewModel
 */
class RegisterViewModel : BaseViewModel() {
    private val loginRepository = InjectorUtil.getLoginRepository()
    val mSmsCodeLiveData = MutableLiveData<Boolean>()//发送验证码的数据观察者
    val mRegisterResultLiveData = MutableLiveData<Boolean>()//注册的数据观察者


    /**
     * 发送验证码
     */
    fun sendSmsCode(phone: String) {
        launchGo({
            val schoolModelWrap = loginRepository.sendSmsCode(phone, "1")
            if (schoolModelWrap.status == 200) {
                //获取验证码成功
                mSmsCodeLiveData.postValue(true)
            } else {
                defUI.toastEvent.postValue(schoolModelWrap.message)
                mSmsCodeLiveData.postValue(false)
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
                    mRegisterResultLiveData.postValue(true)
                } else {
                    mRegisterResultLiveData.postValue(false)
                    defUI.toastEvent.postValue(schoolModelWrap.message)
                }
            }
        )
    }

}