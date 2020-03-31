package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.UserModel
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient

class LoginViewModel : BaseViewModel() {
    //获取用户信息
    val mUserModelData = MutableLiveData<UserModel.Data>()
    val mLoginResult = MutableLiveData<Boolean>()

    private val loginRepository = InjectorUtil.getLoginRepository()

    fun doLogin(phone: String, password: String) {
        launchGo(
            {
                val loginResult = loginRepository.login(phone, password)
                if (loginResult.status == 200) {
                    mUserModelData.postValue(loginResult.data)
                    doChatLogin(phone, password)
                } else {
                    mLoginResult.postValue(false)
                    defUI.toastEvent.postValue("${loginResult.status}:${loginResult.message}")
                }
            }
        )

    }

    /**
     * 登录环信
     */
    private fun doChatLogin(phone: String, password: String) {
        EMClient.getInstance().login(phone, password, object : EMCallBack {
            override fun onSuccess() {
                //加载全部会话
                EMClient.getInstance().chatManager().loadAllConversations()
                defUI.toastEvent.postValue("环信登录成功")
                mLoginResult.postValue(true)
            }

            override fun onProgress(progress: Int, status: String?) {
            }

            override fun onError(code: Int, error: String?) {
                defUI.toastEvent.postValue("环信登录失败")
                mLoginResult.postValue(false)
            }
        })
    }
}