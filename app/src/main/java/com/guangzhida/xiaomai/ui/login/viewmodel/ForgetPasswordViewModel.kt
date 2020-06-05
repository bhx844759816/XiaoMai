package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.Preference
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * 忘记密码
 */
class ForgetPasswordViewModel : BaseViewModel() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    val mSmsCodeLiveData = MutableLiveData<Boolean>()//发送验证码的数据观察者
    val mConfirmPasswordResultLiveData = MutableLiveData<Boolean>()//注册的数据观察者
    private val loginRepository = InjectorUtil.getLoginRepository()
    /**
     * 发送验证码
     */
    fun sendSmsCode(phone: String) {
        launchGo({
            val schoolModelWrap = loginRepository.sendSmsCode(phone, "2")
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
     * 确定修改密码
     */
    fun confirmModifyPassword(phone: String, code: String, password: String) {

        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO){
                    loginRepository.modifyPassword(phone, code, password, password)
                }
                if (result.isSuccess()) {
                    withContext(Dispatchers.IO) {
                        BaseApplication.instance().mUserModel = null
                        mUserGson = ""
                        EMClient.getInstance().logout(true)
                    }
                    defUI.dismissDialog.call()
                    mConfirmPasswordResultLiveData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(result.message)
                    mConfirmPasswordResultLiveData.postValue(false)
                }
            }catch (e:Exception){
                e.printStackTrace()
                mConfirmPasswordResultLiveData.postValue(false)
                defUI.dismissDialog.call()
            }
        }
    }


}