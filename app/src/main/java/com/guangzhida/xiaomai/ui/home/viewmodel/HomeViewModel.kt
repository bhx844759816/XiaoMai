package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.NetworkCheckModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class HomeViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()
    val mAccountModelData = MutableLiveData<AccountModel>()
    val mSchoolModelListData = MutableLiveData<List<SchoolModel>>()
    val bindResult = MutableLiveData<Int>() //绑定结果
    val verifyResultData = MutableLiveData<Boolean>() //认证结果
    val ourtVerifyResultData = MutableLiveData<Boolean>() //退出认证结果
    val modifyPasswordResultData = MutableLiveData<Boolean>() //修改密码结果
    val bindSchoolAccountResultData = MutableLiveData<Boolean>()
    val netWorkCheckResult = MutableLiveData<String>() //网络诊断结果
    val netWorkCheckResult2 = MutableLiveData<NetworkCheckModel>() //网络诊断结果
    var mJob: Job? = null

    //存储本地绑定的账号信息
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")
    //存储学校信息
    private var mSchoolInfoSchoolGson by Preference(Preference.SCHOOL_INFO_GSON, "")

    /**
     * 获取用户信息
     */
    fun getAccountInfo(phone: String) {
        launchGo({
            val accountModel = homeRepository.getAccountInfo(phone)
            if (accountModel.success == "1") {
                mAccountModelData.postValue(accountModel)
                mSchoolAccountInfoGson = Gson().toJson(accountModel)
            }
        }, isShowDialog = false)
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
                    mSchoolInfoSchoolGson = Gson().toJson(schoolModelWrap.result)
                }
            }, isShowDialog = false
        )
    }

    /**
     * 绑定手机号
     */
    fun bindAccount(account: String, password: String) {
        launchUI {
            try {
                val accountModel = withContext(Dispatchers.IO) {
                    homeRepository.getAccountInfo(account)
                }
                if (accountModel.success == "1") {//有这个用户
                    if (password == accountModel.pass) {
                        mSchoolAccountInfoGson = Gson().toJson(accountModel)
                        mAccountModelData.postValue(accountModel)
                        bindResult.postValue(1) //绑定成功
                    } else {
                        bindResult.postValue(-1) //密码错误
                    }
                } else {
                    bindResult.postValue(0) //没有这个用户
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bindResult.postValue(-2)
            }

        }

    }

    /**
     * 网络诊断
     */
    fun doNetWorkCheck(isWifi: Boolean) {
        mJob = launchUI {
            try {
                val ip = NetworkUtils.getIPAddress(true);
                val content = StringBuilder()
                var isCheckSuccess: Boolean
                if (isWifi) {
                    //网关ip
                    val gatewayIp = ip.substring(0, ip.lastIndexOf(".")).plus(".1")
                    LogUtils.i("网关ip=$gatewayIp")
                    //首先ping内网
                    val intranetResultModel = withContext(Dispatchers.IO) {
                        NetworkDiagnosisManager.executeCmd2(gatewayIp, 3, 100)
                    }
                    isCheckSuccess = intranetResultModel.success
                    val pingGatewayResult = buildString {
                        append("本机 - 网关")
                        append("\n")
                        append("ping值:")
                        append(intranetResultModel.averageDelay)
                        append("\n")
                        append("丢包率:")
                        append(intranetResultModel.lossPackageRate)
                        append("\n")
                    }
                    content.append(pingGatewayResult)
                    content.append("\n")
                }
                //ping外网
                val extraNetResultModel = withContext(Dispatchers.IO) {
                    //ping阿里的公网ip地址
                    NetworkDiagnosisManager.executeCmd2("122.51.167.92", 3, 100)
                }
                isCheckSuccess = extraNetResultModel.success
                val extraNetResult = buildString {
                    append("本机 - 服务器")
                    append("\n")
                    append("ping值:")
                    append(extraNetResultModel.averageDelay)
                    append("\n")
                    append("丢包率:")
                    append(extraNetResultModel.lossPackageRate)
                }
                content.append(extraNetResult)
                content.append("\n")
                //解析服务器 检测DNS解析是否有问题
                val domainAddress = withContext(Dispatchers.IO) {
                    NetworkUtils.getDomainAddress("www.baidu.com")
                }
                isCheckSuccess = domainAddress != null && domainAddress.isNotEmpty()
//              if (!intranetResultModel.success || !extraNetResultModel.success || domainAddress.isNullOrEmpty()) {
//              } else {
//                  content.append("检测网络正常")
//              }
                val model = NetworkCheckModel(
                    content = content.toString(),
                    checkSuccess = isCheckSuccess
                )
                netWorkCheckResult2.postValue(model)
            } catch (e: Exception) {
                val model = NetworkCheckModel(
                    content = "检测异常",
                    checkSuccess = false
                )
                netWorkCheckResult2.postValue(model)
                e.printStackTrace()
            }
        }
    }

    /**
     * 取消网络诊断
     */
    fun cancelNetWorkCheck() {
        LogUtils.i("cancelNetWorkCheck")
        mJob?.cancel()
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
                    defUI.toastEvent.postValue(model.msg ?: "认证失败")
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
                if (model.code == 0) {//
                    ourtVerifyResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(model.msg ?: "退出认证失败")
                    ourtVerifyResultData.postValue(false)
                }
            }
        )
    }


    /**
     * 绑定校园卡套餐
     */
    fun bindSchoolAccount(
        account: String,
        proxy_user: String,
        proxy_vlan: String,
        apiPass: String
    ) {
        launchUI {
            try {
                defUI.showDialog.call()
                val url = "http://yonghu.guangzhida.cn/lfradius/api.php"
                val params = mapOf(
                    "user" to account,
                    "proxy_user" to proxy_user,
                    "proxy_pass" to "100861",
                    "proxy_vlan" to proxy_vlan,
                    "disconnect" to "0",
                    "type" to "editproxyuser",
                    "run" to "userinfo",
                    "apipass" to apiPass
                )
                val result = withContext(Dispatchers.IO) {
                    val responseBody = homeRepository.bindSchoolAccount(url, params)
                    responseBody.string()
                }
                if (result.contains("success") && result.contains("1")) {
                    val accountModel = withContext(Dispatchers.IO) {
                        homeRepository.getAccountInfo(account)
                    }
                    if (accountModel.success == "1") {
                        mSchoolAccountInfoGson = Gson().toJson(accountModel)
                        mAccountModelData.postValue(accountModel)
                    }
                    bindSchoolAccountResultData.postValue(true)
                    defUI.toastEvent.postValue("绑定校园卡账号成功")
                } else {
                    bindSchoolAccountResultData.postValue(false)
                    defUI.toastEvent.postValue("绑定校园卡账号失败")
                }
                defUI.dismissDialog.call()
            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.dismissDialog.call()
            }
        }


    }

    /**
     * 修改密码
     */
    fun modifyPassword(account: String, password: String, apiPass: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val url = "http://yonghu.guangzhida.cn/lfradius/api.php"
                val params = mapOf(
                    "user" to account,
                    "pass" to password,
                    "disconnect" to "1",
                    "type" to "editpassword",
                    "run" to "userinfo",
                    "apipass" to apiPass
                )
                val result = withContext(Dispatchers.IO) {
                    val responseBody = homeRepository.modifyAccountPassword(url, params)
                    responseBody.string()
                }
                if (result.contains("success") && result.contains("1")) {
                    //修改成功
                    defUI.toastEvent.postValue("修改密码成功，请牢记修改后的密码")
                    modifyPasswordResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue("修改密码失败")
                    modifyPasswordResultData.postValue(false)
                }
                defUI.dismissDialog.call()
            } catch (e: Exception) {
                e.printStackTrace()
                defUI.dismissDialog.call()
            }
        }
    }
}