package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.NetworkCheckModel
import com.guangzhida.xiaomai.model.PopAdModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.*
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class HomeViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()
    private val loginRepository = InjectorUtil.getLoginRepository()
    private val chatRepository = InjectorUtil.getChatRepository()
    val mAccountModelData = MutableLiveData<AccountModel>()
    val mSchoolModelListData = MutableLiveData<List<SchoolModel>>()
    val bindResult = MutableLiveData<Int>() //绑定结果
    val verifyResultData = MutableLiveData<Boolean>() //认证结果
    val ourtVerifyResultData = MutableLiveData<Boolean>() //退出认证结果
    val modifyPasswordResultData = MutableLiveData<Boolean>() //修改密码结果
    val bindSchoolAccountResultData = MutableLiveData<Boolean>() //确认绑定校园卡结果
    val modifySchoolNameResultData = MutableLiveData<Boolean>() //修改账号的校园卡
    val netWorkCheckResult = MutableLiveData<NetworkCheckModel>() //网络诊断结果
    val popAdResultData = MutableLiveData<PopAdModel>()//弹窗广告
    //用户对象
    private var mUserGson by Preference(Preference.USER_GSON, "")
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
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
                BaseApplication.instance().mAccountModel = accountModel
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
                    mSchoolModelListData.postValue(schoolModelWrap.data)
                    mSchoolInfoSchoolGson = Gson().toJson(schoolModelWrap.data)
                }
            }, isShowDialog = false
        )
    }


    /**
     * 当未登录的时候 调用校园卡账号密码查询服务器是否有这个用户有的话登录
     * //加载好友列表 登录环信
     * 当已登录的时候 检测UserModel的校园卡账号和密码是否为空 为空调用绑定校园卡的接口
     *
     * 绑定校园卡
     */
    fun bindAccount(account: String, password: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val accountModel = withContext(Dispatchers.IO) {
                    homeRepository.getAccountInfo(account)
                }
                if (accountModel.success == "1") {//有这个用户
                    if (password == accountModel.pass) {
                        mSchoolAccountInfoGson = Gson().toJson(accountModel)
                        BaseApplication.instance().mAccountModel = accountModel
                        mAccountModelData.postValue(accountModel)
                        if (BaseApplication.instance().mUserModel == null) {
                            //隐形调用登录方法尝试进行登录
                            val loginResult = withContext(Dispatchers.IO) {
                                //
                                val userModel =
                                    homeRepository.doLoginBySchoolAccount(account, password)
                                if (userModel.status == 200) {
                                    BaseApplication.instance().mUserModel = userModel.data
                                    mUserGson = Gson().toJson(userModel.data)
                                    userModelChangeLiveData.postValue(true)
                                    //拉取好友到本地
                                    loadServiceContactsList()
                                }
                                userModel.status == 200
                            }
                            if (loginResult) {
                                doChatLogin(
                                    BaseApplication.instance().mUserModel!!.username, password
                                )
                            } else {
                                bindResult.postValue(1) //绑定成功
                            }
                        } else if (BaseApplication.instance().mUserModel!!.campusNetworkNum.isNullOrEmpty() ||
                            BaseApplication.instance().mUserModel!!.campusNetworkPwd.isNullOrEmpty()
                        ) {
                            //调用绑定校园卡的接口
                            withContext(Dispatchers.IO) {
                                homeRepository.doBindSchoolAccount(
                                    account,
                                    password,
                                    BaseApplication.instance().mUserModel!!.id
                                )
                            }
                            bindResult.postValue(1) //绑定成功
                        } else {
                            bindResult.postValue(1) //绑定成功
                        }
                    } else {
                        bindResult.postValue(-1) //密码错误
                    }
                } else {
                    bindResult.postValue(0) //没有这个用户
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bindResult.postValue(-2)
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }

    /**
     * 加载后台服务
     */
    private suspend fun loadServiceContactsList() {
        return withContext(Dispatchers.IO) {
            val result = chatRepository.getFriendList()
            val userEntityList = mUserDao?.queryAll()
            if (result.isSuccess()) {
                val list = result.data.map { chatUserModel ->
                    val localUserEntity = userEntityList?.find {
                        it.uid == chatUserModel.id.toLong()
                    }
                    UserEntity(
                        uid = chatUserModel.id.toLong(),
                        nickName = chatUserModel.nickName,
                        userName = chatUserModel.mobilePhone,
                        avatarUrl = chatUserModel.headUrl ?: "",
                        age = chatUserModel.age.toString(),
                        sex = chatUserModel.sex.toString(),
                        singUp = chatUserModel.signature ?: "",
                        remarkName = localUserEntity?.remarkName ?: ""
                    )
                }
                //删除服务器未存在的本地好友信息
                userEntityList?.forEach {
                    mUserDao?.delete(it)
                }
                list.forEach {
                    mUserDao?.insert(it)
                }
            }
        }
    }

    /**
     * 登录环信
     */
    private fun doChatLogin(phone: String, password: String) {
        //首先退出登录
        EMClient.getInstance().logout(false)
        //然后登录
        EMClient.getInstance().login(phone, password, object : EMCallBack {
            override fun onSuccess() {
                //加载全部会话
                EMClient.getInstance().chatManager().loadAllConversations()
                bindResult.postValue(1) //绑定成功
            }

            override fun onProgress(progress: Int, status: String?) {
            }

            override fun onError(code: Int, error: String?) {
                ToastUtils.toastShort("聊天服务器登录失败")
                bindResult.postValue(1) //绑定成功
            }
        })
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
                        NetworkDiagnosisManager.executeCmd2(gatewayIp, 3)
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
                    NetworkDiagnosisManager.executeCmd2("122.51.167.92", 3)
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
                netWorkCheckResult.postValue(model)
            } catch (e: Exception) {
                val model = NetworkCheckModel(
                    content = "检测异常",
                    checkSuccess = false
                )
                netWorkCheckResult.postValue(model)
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

    fun doAccountVerify(
        verifyUrl: String,
        quitVerifyUrl: String,
        accountModel: AccountModel
    ) {
        launchUI {
            try {
                defUI.showDialog.call()
//                //首先退出认证然后在调用认证接口
                withContext(Dispatchers.IO) {
                    val params = mapOf(
                        "wlanuserip" to NetworkUtils.getIPAddress(true),
                        "wlanapmac" to NetworkUtils.getMac()
                    )
                    homeRepository.exitAccountVerify(quitVerifyUrl, params)
                }
                val model = withContext(Dispatchers.IO) {
                    val params = mapOf(
                        "wlanuserip" to NetworkUtils.getIPAddress(true),
                        "wlanapmac" to NetworkUtils.getMac(),
                        "username" to accountModel.user,
                        "password" to accountModel.pass,
                        "line" to "4"
                    )
                    homeRepository.doAccountVerify(verifyUrl, params)
                }
                if (model.code == 200) {//
                    verifyResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(model.msg)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                defUI.toastEvent.postValue("连接超时，请稍后重试")
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }

    /**
     * 退出认证
     */
    fun quitAccountVerify(url: String) {
        launchGo(
            {
                val params = mapOf(
                    "wlanuserip" to NetworkUtils.getIPAddress(true),
                    "wlanapmac" to NetworkUtils.getMac()
                )
                val model = homeRepository.exitAccountVerify(url, params)
                if (model.code == 0) {//
                    ourtVerifyResultData.postValue(true)
                } else {
                    defUI.toastEvent.postValue(model.msg)
                    ourtVerifyResultData.postValue(false)
                }
            }
        )
    }

    /**
     * 更改凌风后台账号绑定的学校
     */
    fun bindAccountSchoolName(account: String, schoolName: String, apiPass: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val url = "http://yonghu.guangzhida.cn/lfradius/api.php"
                val params = mapOf(
                    "user" to account,
                    "name" to schoolName,
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
                    modifySchoolNameResultData.postValue(true)
                    defUI.toastEvent.postValue("修改校园卡绑定学校成功")
                } else {
                    modifySchoolNameResultData.postValue(false)
                    defUI.toastEvent.postValue("修改校园卡绑定学校失败")
                }
            } catch (t: Throwable) {

            } finally {
                defUI.dismissDialog.call()
            }
        }
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

            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
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

    /**
     * 通过学校ID获取弹窗广告
     */
    fun getPopAdBySchoolId(schoolId: String) {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    homeRepository.getPopAdBySchoolId(schoolId)
                }
                if (result.isSuccess()) {
                    popAdResultData.postValue(result.data)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

}