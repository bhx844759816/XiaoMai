package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.PingResultModel
import com.guangzhida.xiaomai.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel : BaseViewModel() {

    private val homeRepository = InjectorUtil.getHomeRepository()

    val mAccountModelData = MutableLiveData<AccountModel>()
    val bindResult = MutableLiveData<Boolean>() //绑定结果
    val netWorkCheckResult = MutableLiveData<Pair<String, PingResultModel>>()
    /**
     * 获取用户信息
     */
    fun getAccountInfo(phone: String) {
        launchGo({
            val accountModel = homeRepository.getAccountInfo(phone)
            if (accountModel.success == "1") {
                mAccountModelData.postValue(accountModel)
            }
        }, isShowDialog = false)
    }

    /**
     * 绑定手机号
     */
    fun bindAccount(account: String, password: String) {
        if (account == "0" && password == "0") {
            bindResult.postValue(true)
        } else {
            bindResult.postValue(false)
        }

    }

    fun doNetWorkCheck() {
        launchUI {
            try {
                val ip = NetworkUtils.getIPAddress(true);
                if (ip.isNotEmpty()) {
                    //首先ping内网
                    val intranetResultModel = withContext(Dispatchers.IO) {
                        LogUtils.i("内网IP=$ip")
                        NetworkDiagnosisManager.executeCmd2(ip, 4, 100)
                    }
                    //发送内网ping的结果
                    netWorkCheckResult.postValue(Pair("内网", intranetResultModel))
                }
                //ping外网
                val extraNetResultModel = withContext(Dispatchers.IO) {
                    //ping阿里的公网ip地址
                    NetworkDiagnosisManager.executeCmd2("223.5.5.5", 4, 100)
                }
                //发送外网ping的结果
                netWorkCheckResult.postValue(Pair("外网", extraNetResultModel))
                //解析DNS
                val domainAddress = withContext(Dispatchers.IO) {
                    NetworkUtils.getDomainAddress("www.baidu.com")
                }
                val dnsCheckResult =
                    Pair("公网", PingResultModel(success = !domainAddress.isNullOrEmpty()))
                netWorkCheckResult.postValue(dnsCheckResult)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 取消网络诊断1
     */
    fun cancelNetWorkCheck() {
        NetworkDiagnosisManager.cancelExecute()
    }
}