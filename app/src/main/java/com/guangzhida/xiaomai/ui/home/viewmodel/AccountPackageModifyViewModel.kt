package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.PackageInfoModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * 套餐修改
 */
class AccountPackageModifyViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()
    val mPackageInfoObserver = MutableLiveData<List<PackageInfoModel>>()
    val mClearPackageObserver = MutableLiveData<Boolean>()
    val mAccountModelData = MutableLiveData<AccountModel>()
    //存储本地绑定的账号信息
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")

    /**
     * 获取套餐信息
     */
    fun getPackageInfo(phone: String) {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    val result = homeRepository.getPackageInfo(phone)
                    result.string()
                }
                if (result.contains("\"success\":\"1\"")) {
                    val list = mutableListOf<PackageInfoModel>()
                    val splits = result.split("]")
                    splits.subList(1, splits.size).forEach {
                        val model = PackageInfoModel()
                        val splitLittle = it.split(",")
                        splitLittle.forEach {
                            if (it.contains(":")) {
                                val splitLittle_ = it.split(":")
                                LogUtils.i(splitLittle_.toString())
                                val key = splitLittle_[0].replace("\"", "")
                                val value = splitLittle_[1].replace("\"", "")
//                        "id":"21","servername":"免费用户（1M）","proxy_type":"","price":"0.10","current_use":"1"
                                when (key) {
                                    "id" -> {
                                        model.id = value
                                    }
                                    "servername" -> {
                                        model.servername = value
                                    }
                                    "proxy_type" -> {
                                        model.proxy_type = value
                                    }
                                    "price" -> {
                                        model.price = value
                                    }
                                    "current_use" -> {
                                        model.current_use = value
                                    }
                                }
                            }
                        }
                        list.add(model)
                    }
                    mPackageInfoObserver.postValue(list)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 清空套餐信息
     */
    fun clearAccountPackage(user: String, apipass: String) {
        launchUI {
            val url = "http://yonghu.guangzhida.cn/lfradius/api.php"
            val params = mapOf(
                "user" to user,
                "disconnect" to "1",
                "type" to "server_clearing",
                "run" to "server",
                "apipass" to apipass
            )
            val result = withContext(Dispatchers.IO) {
                val response = homeRepository.clearAccountPackage(url, params)
                response.string()
            }
            if (result.contains("success") && result.contains("1")) {
                val accountModel = withContext(Dispatchers.IO) {
                     homeRepository.getAccountInfo(user)
                }
                if (accountModel.success == "1") {
                    mAccountModelData.postValue(accountModel)
                    mSchoolAccountInfoGson = Gson().toJson(accountModel)
                    mClearPackageObserver.postValue(true)
                    ToastUtils.toastLong("清除套餐成功")
                }
            } else {
                mClearPackageObserver.postValue(false)
                ToastUtils.toastLong("清除套餐失败")
            }
        }
    }
}