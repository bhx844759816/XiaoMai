package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.data.login.LoginNetwork
import com.guangzhida.xiaomai.data.login.LoginRepository
import com.guangzhida.xiaomai.model.UserModel

class LoginViewModel : BaseViewModel() {
    //获取用户信息
    val mUserModelData = MutableLiveData<UserModel.Data>()

    private val loginRepository = InjectorUtil.getLoginRepository()

    fun doLogin(phone: String, password: String) {

        launchGo(
            {
                val result = loginRepository.login(phone, password)
                if (result.status == 200) {
                    mUserModelData.postValue(result.data)
                }
            }
        )
    }
}