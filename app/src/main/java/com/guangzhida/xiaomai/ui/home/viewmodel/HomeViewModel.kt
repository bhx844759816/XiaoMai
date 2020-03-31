package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.PingResultModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class HomeViewModel : BaseViewModel() {

    private val homeRepository = InjectorUtil.getHomeRepository()

    val mAccountModelData = MutableLiveData<AccountModel>()
    val mSchoolModelData = MutableLiveData<SchoolModel>()
    val mSchoolModelListData = MutableLiveData<List<SchoolModel>>()
    val bindResult = MutableLiveData<Int>() //绑定结果
    val verifyResultData = MutableLiveData<Boolean>() //绑定结果
    val ourtVerifyResultData = MutableLiveData<Boolean>() //绑定结果
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
     * 获取学校信息
     * @param schoolName 学校名称
     */
    fun getSchoolInfo(schoolName: String) {
        launchGo(
            {
                val schoolModel = homeRepository.getSchoolInfoByName(schoolName)
                LogUtils.i("SchoolModel=$schoolModel")
                if (schoolModel.status == 200) {
                    mSchoolModelData.postValue(schoolModel.data)
                }
            }, isShowDialog = false
        )
    }

    /**
     * 获取全部学校信息
     */
    fun getAllSchoolInfo() {
        launchGo(
            {
                val schoolModelWrap = homeRepository.getSchoolInfo()
                if (schoolModelWrap.status == 200) {
                    mSchoolModelListData.postValue(schoolModelWrap.result)
                }
            }, isShowDialog = false
        )
    }

    /**
     * 绑定手机号
     */
    fun bindAccount(account: String, password: String) {
        launchUI {
            val accountModel = withContext(Dispatchers.IO) {
                homeRepository.getAccountInfo(account)
            }
            if (accountModel.success == "1") {//有这个用户
                if (password == accountModel.pass) {
                    bindResult.postValue(1) //绑定成功
                    mAccountModelData.postValue(accountModel)
                } else {
                    bindResult.postValue(-1) //密码错误
                }
            } else {
                bindResult.postValue(0) //没有这个用户
            }
        }

    }

    /**
     * 网络诊断
     */
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

    /**
     * 一键认证
     */
    fun doAccountVerify(url: String, params: Map<String, String?>) {
        launchGo(
            {
                val model = homeRepository.doAccountVerify(url, params)
                LogUtils.i("doAccountVerify=$model")
                if (model.code == 200) {//
                    verifyResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(model.data.errMessage ?: "认证失败")
                }
            }
        )
    }

    /**
     * 退出认证
     */
    fun quitAccountVerify(url: String, params: Map<String, String?>) {
        launchGo(
            {
                val model = homeRepository.exitAccountVerify(url, params)
                if (model.code == 200) {//
                    ourtVerifyResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(model.data.errMessage ?: "退出认证失败")
                    ourtVerifyResultData.postValue(false)
                }
            }
        )
    }
}