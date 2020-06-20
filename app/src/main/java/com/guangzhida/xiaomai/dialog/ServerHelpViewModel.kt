package com.guangzhida.xiaomai.dialog

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil

/**
 *
 */
class ServerHelpViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()

    val mQueryAccountInfoResult = MutableLiveData<Boolean>()


    /**
     * 获取用户信息
     */
    fun getAccountInfo(phone: String) {
        launchGo({
            val accountModel = homeRepository.getAccountInfo(phone)
            if (accountModel.success == "1") {
                //有这个账号
            } else {
                //无这个账号
            }
        }, isShowDialog = false)
    }

}