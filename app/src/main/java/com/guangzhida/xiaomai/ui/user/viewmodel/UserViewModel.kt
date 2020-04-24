package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()
    val bindResult = MutableLiveData<Int>() //绑定结果

//    /**
//     * 绑定手机号
//     */
//    fun bindAccount(account: String, password: String) {
//        launchUI {
//            val accountModel = withContext(Dispatchers.IO) {
//                homeRepository.getAccountInfo(account)
//            }
//            if (accountModel.success == "1") {//有这个用户
//                if (password == accountModel.pass) {
//                    mAccountModelData.postValue(accountModel)
//                    mSchoolAccountInfoGson = Gson().toJson(accountModel)
//                    bindResult.postValue(1) //绑定成功
//                } else {
//                    bindResult.postValue(-1) //密码错误
//                }
//            } else {
//                bindResult.postValue(0) //没有这个用户
//            }
//        }
//
//    }
//
//    /**
//     * 绑定校园卡套餐
//     */
//    fun bindSchoolAccount(
//        account: String,
//        proxy_user: String,
//        proxy_vlan: String,
//        apiPass: String
//    ) {
//        launchUI {
//            try {
//                defUI.showDialog.call()
//                val url = "http://yonghu.guangzhida.cn/lfradius/api.php"
//                val params = mapOf(
//                    "user" to account,
//                    "proxy_user" to proxy_user,
//                    "proxy_pass" to "100861",
//                    "proxy_vlan" to proxy_vlan,
//                    "disconnect" to "0",
//                    "type" to "editproxyuser",
//                    "run" to "userinfo",
//                    "apipass" to apiPass
//                )
//                val result = withContext(Dispatchers.IO) {
//                    val responseBody = homeRepository.bindSchoolAccount(url, params)
//                    responseBody.string()
//                }
//                if (result.contains("success") && result.contains("1")) {
//                    val accountModel = withContext(Dispatchers.IO) {
//                        homeRepository.getAccountInfo(account)
//                    }
//                    if (accountModel.success == "1") {
//                        mSchoolAccountInfoGson = Gson().toJson(accountModel)
//                        mAccountModelData.postValue(accountModel)
//                    }
//                    defUI.toastEvent.postValue("绑定校园卡账号成功")
//                } else {
//                    defUI.toastEvent.postValue("绑定校园卡账号失败")
//                }
//                defUI.dismissDialog.call()
//            } catch (e: Throwable) {
//                e.printStackTrace()
//                defUI.dismissDialog.call()
//            }
//        }
//
//
//    }

}