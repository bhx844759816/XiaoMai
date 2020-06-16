package com.guangzhida.xiaomai.ui.home

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guangzhida.xiaomai.*
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.dialog.*
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey.SCHOOL_MODEL_CHANGE_KEY
import com.guangzhida.xiaomai.event.netChangeLiveData
import com.guangzhida.xiaomai.event.schoolModelChangeLiveData
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.ext.loadImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.ktxlibrary.ext.permission.request
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.ui.VerifyWebActivity
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.chat.ServiceActivity
import com.guangzhida.xiaomai.ui.home.viewmodel.HomeViewModel
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.ui.user.UserActivity
import com.guangzhida.xiaomai.utils.*
import com.guangzhida.xiaomai.view.VerifyInternetManager
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_home_center_grid.*

/**
 * 首页校园网页面
 */
class HomeFragment : BaseFragment<HomeViewModel>() {
    private val mAccountInfoMaps = mutableMapOf<String, String>()
    private var mAccountModel: AccountModel? = BaseApplication.instance().mAccountModel
    private var mSchoolModelList: List<SchoolModel>? = null
    private var mSchoolModel: SchoolModel? = null
    private var mBindAccount: String? = null
    private var mBindAccountPassword: String? = null
    private var isModifyPackage = false
    //存储本地绑定的账号信息
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")
    //存储学校信息
    private var mSchoolInfoGson by Preference(Preference.SCHOOL_INFO_GSON, "")
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mGson by lazy {
        Gson()
    }

    override fun layoutId(): Int = R.layout.fragment_home

    override fun initView(savedInstanceState: Bundle?) {
        initListener()
        registerLiveData()
        //获取本地存储的学校信息
        mSchoolModelList = mGson.fromJson<List<SchoolModel>>(mSchoolInfoGson, object :
            TypeToken<List<SchoolModel>>() {
        }.type)
        //获取选择的学校信息
        mSchoolModel = mGson.fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
        //获取所有学校的信息
        viewModel.getAllSchoolInfo()
        mAccountModel?.let { accountModel ->
            viewModel.getAccountInfo(accountModel.user)
        }
        //初始化网络认证的管理器
        mSchoolModel?.let {
            viewModel.getPopAdBySchoolId(it.id)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }


    private fun initData() {
        request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            onGranted {
                val isWifiConnect = NetworkUtils.isWifiConnected(context)
                val mWifiName = NetworkUtils.getWifiName(activity)
                val mIpAddress = NetworkUtils.getIPAddress(true)
                mAccountInfoMaps["wifi名称"] = ""
                mAccountInfoMaps["IP地址"] = ""
                mAccountInfoMaps["账号"] = mAccountModel?.user ?: ""
                mAccountInfoMaps["使用套餐"] = mAccountModel?.servername ?: ""
                mAccountInfoMaps["到期时间"] = mAccountModel?.expiretime ?: ""
                tvSchoolName.text = mSchoolModel?.name ?: "请选择您的学校"
                if (isWifiConnect) {
                    mAccountInfoMaps["wifi名称"] = mWifiName
                    mAccountInfoMaps["IP地址"] = mIpAddress
                }
                checkNotifyRemind()
                updateTopCardInfo()
                checkAccountState()
            }
            onShowRationale {
                it.retry()
            }
        }

    }


    /**
     * 接口监听
     */
    override fun initListener() {
        //绑定账号
        idBindAccount.setOnClickListener {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请选择学校")
                showSelectSchoolDialog()
            } else {
                showBindAccountDialog()
            }
        }
        //用户注册
        llRegister.setOnClickListener {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请先选择学校")
                showSelectSchoolDialog()
            } else {
                LogUtils.i("注册url=${mSchoolModel!!.regiestNewUser}")
                startKtxActivity<WebActivity>(
                    values = listOf(
                        Pair("url", mSchoolModel!!.regiestNewUser),
                        Pair("type", "AccountRegister")
                    )
                )
            }
        }
        //连接wifi
        idConnectWifiBtn.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }
        //选择学校
        rlSelectSchool.setOnClickListener {
            activity?.let {
                if (mSchoolModelList.isNullOrEmpty()) {
                    viewModel.getAllSchoolInfo()
                } else {
                    showSelectSchoolDialog()
                }
            }
        }

        //查询余额
        llQueryBalance.setOnClickListener {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请先选择学校")
                showSelectSchoolDialog()
            } else {
                val url =
                    "${BASE_URL}network/activity/goAddSchoolCard?schoolId=${mSchoolModel?.id}&schoolName=${mSchoolModel?.name}"
//                val url = "192.168.1.74:8762/api/admin/network/activity/goAddSchoolCard?schoolId=${mSchoolModel?.id}&schoolName=${mSchoolModel?.name}"
                LogUtils.i("url=$url")
                startKtxActivity<WebActivity>(
                    values = listOf(
                        Pair("url", url),
                        Pair("type", "activity")
                    )
                )
            }

        }
        //账户充值
        llAccountRecharge.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                jumpToAccountRecharge()
            }
        }
        //套餐修改
        llAccountPackageModify.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                isModifyPackage = true
                startKtxActivityForResult<AccountPackageModifyActivity>(
                    requestCode = 0x01, values = listOf(
                        Pair("findSecret", mSchoolModel!!.findSecret),
                        Pair("AccountModel", mAccountModel!!)
                    )
                )
            }
        }
        //关于密码
        aboutPassword.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                activity?.let {
                    AboutPasswordDialog.showDialog(it, it) { index ->
                        if (index == 0) {
                            ModifyPasswordDialog.showDialog(it, it) { _, newPsd ->
                                mBindAccountPassword = newPsd
                                viewModel.modifyPassword(
                                    mAccountModel!!.user,
                                    newPsd,
                                    mSchoolModel?.findSecret ?: ""
                                )
                            }
                        } else if (index == 1) {
                            val intent = Intent(context, WebActivity::class.java)
                            intent.putExtra("url", mSchoolModel?.findPwd)
                            intent.putExtra("type", "ForgetPassword")
                            startActivity(intent)
                        }
                    }
                }
            }
        }
        //我的客服
        llService.setOnClickListener {
            startActivity(Intent(context, ServiceActivity::class.java))
        }
        //网络诊断
        llNetworkDiagnosis.setOnClickListener {
            activity?.let {
                NetworkCheckDialog2.showDialog(it, it) {
                    viewModel.cancelNetWorkCheck()
                }
                viewModel.doNetWorkCheck(NetworkUtils.isWifiConnected(context))
            }
        }
        //一键认证
        tvVerify.setOnClickListener {
            if (!NetworkUtils.isWifiConnected(context)) {
                ToastUtils.toastShort("请先连接wifi")
                return@setOnClickListener
            }
            if (checkAccountAndSchoolIsExit()) {
//                startKtxActivity<VerifyWebActivity>()
                activity?.let {
                    viewModel.defUI.showDialog.call()
                    VerifyInternetManager.doVerify(
                        it,
                        llWebParent,
                        mAccountModel?.user ?: "",
                        mAccountModel?.pass ?: ""
                    ) { _, message ->
                        viewModel.defUI.toastEvent.postValue(message)
                        viewModel.defUI.dismissDialog.postValue(null)
                    }
                }
            }
        }
        //退出认证
        tvQuitVerify.setOnClickListener {
            if (!NetworkUtils.isWifiConnected(context)) {
                ToastUtils.toastShort("请先连接wifi")
                return@setOnClickListener
            }
            if (mAccountModel != null && mAccountModel!!.online == "0") {
                ToastUtils.toastShort("未认证请先认证")
                return@setOnClickListener
            }
            if (checkAccountAndSchoolIsExit()) {
                viewModel.quitAccountVerify(mSchoolModel!!.logoutLogin)
            }
        }
    }

    /**
     * 注册观察者
     */
    private fun registerLiveData() {
        //获取账号信息
        viewModel.mAccountModelData.observe(this, Observer {
            mAccountModel = it
            mAccountInfoMaps["账号"] = it.user
            mAccountInfoMaps["使用套餐"] = it.servername
            mAccountInfoMaps["到期时间"] = it.expiretime
            updateTopCardInfo()
        })
        //绑定账号结果
        viewModel.bindResult.observe(this, Observer {
            when (it) {
                0 -> {//没有这个账号
                    ToastUtils.toastShort("查无此账号")
                }
                -1 -> {//密码错误
                    ToastUtils.toastShort("密码错误请重新输入")
                }
                1 -> {//绑定成功
                    ToastUtils.toastShort("绑定成功")
                    BindAccountDialog.dismissDialog()
                    mAccountModel?.let { accountModel ->
                        if (accountModel.servername.isEmpty()) {
                            showNoSetMealDialog("您未购买套餐,请先购买套餐")
                        } else {
                            val timeSpace = DateUtils.getTwoDay(
                                mAccountModel!!.expiretime,
                                DateUtils.dateToStr(DateUtils.getNow())
                            )
                            if (timeSpace < 0) {
                                showNoSetMealDialog("您的套餐已过期,请先充值")
                                showNoSetMealNotifyView()
                            } else if (accountModel.name != mSchoolModel!!.name) {
                                //绑定账号的学校和选择的学校不一致
                                val content = buildString {
                                    append("您选择的校区: ")
                                    append(mSchoolModel!!.name)
                                    append("\n")
                                    append("您账号注册区域: ")
                                    append(accountModel.name)
                                    append("\n")
                                    append("请选择正确的校区")
                                }
                                showSchoolBindAccountTipsDialog(content)
                                //你选择的校区 树青
                                //你注册的校区 铁道
                                //是否修改为注册校区铁道，是的话
//                                ToastUtils.toastLong("账号注册区域和选择的学校不一致，请选择正确的校区或联系客服")
                            } else {
                                //提示是否是校园卡账号然后进行绑定
                                if (accountModel.servername.contains("中国")) {
                                    showBindSchoolAccountDialog()
                                }
                            }
                        }
                    }
                }
                -2 -> {
                    ToastUtils.toastShort("网络错误")
                }
            }
        })

        //网络检测结果
        viewModel.netWorkCheckResult.observe(this, Observer {
            LogUtils.i("网络检测结果=$it")
            NetworkCheckDialog2.setNetworkCheckContent(it)
        })
        //一键认证结果
        viewModel.verifyResultData.observe(this, Observer {
            if (it) {
                ToastUtils.toastShort("认证成功")
            } else {
                ToastUtils.toastShort("认证失败")
            }
        })
        //退出认证
        viewModel.ourtVerifyResultData.observe(this, Observer {
            if (it) {
                ToastUtils.toastShort("退出认证成功")
            } else {
                ToastUtils.toastShort("退出认证失败")
            }
        })
        //获取校园列表
        viewModel.mSchoolModelListData.observe(this, Observer {
            mSchoolModelList = it
        })
        //修改密码成功
        viewModel.modifyPasswordResultData.observe(this, Observer {
            if (it) {
                ModifyPasswordDialog.dismissDialog()
            }
        })
        //绑定校园卡结果
        viewModel.bindSchoolAccountResultData.observe(this, Observer {

        })
        viewModel.modifySchoolNameResultData.observe(this, Observer {
            tvSchoolName.text = mSchoolModel?.name
        })
        //网络状态切换
        netChangeLiveData.observe(this, Observer {
            if (it) {
                val mWifiName = NetworkUtils.getWifiName(activity)
                val mIpAddress = NetworkUtils.getIPAddress(true)
                mAccountInfoMaps["wifi名称"] = mWifiName
                mAccountInfoMaps["IP地址"] = mIpAddress
            } else {
                mAccountInfoMaps["wifi名称"] = ""
                mAccountInfoMaps["IP地址"] = ""
            }
            updateTopCardInfo()
        })
        //学校信息改变
        schoolModelChangeLiveData.observe(this, Observer {
            mSchoolModel = it
            tvSchoolName.text = mSchoolModel?.name
        })
        //获取到广告
        viewModel.popAdResultData.observe(this, Observer { adModel ->
            if (adModel != null) {
                activity?.let { activity ->
                    AppPopAdDialog.showDialog(activity, activity, adModel)
                }
            }
        })
        //选择的学校改变的
        LiveDataBus.with(SCHOOL_MODEL_CHANGE_KEY, SchoolModel::class.java).observe(this, Observer {
            mSchoolModel = it
            tvSchoolName.text = mSchoolModel?.name
        })
    }


    /**
     * 检测账号的状态 是否购买套餐，是否套餐过期
     */
    private fun checkAccountState() {
        mAccountModel?.let { accountModel ->
            if (accountModel.servername.isEmpty()) {
                showNoSetMealDialog("您未购买套餐,请先购买套餐")
            } else {
                if (accountModel.proxy_user == "" && accountModel.servername.contains("中国")) {
                    //提示是否是校园卡账号然后进行绑定
                    showBindSchoolAccountDialog()
                } else {
                    val timeSpace = DateUtils.getTwoDay(
                        mAccountModel!!.expiretime,
                        DateUtils.dateToStr(DateUtils.getNow())
                    )
                    if (timeSpace < 0) {
                        showNoSetMealNotifyView()
                    } else {
                        llNotifyParent.gone()
                    }
                }
            }
        }
    }

    /**
     * 展示套餐过期的通知
     */
    private fun showNoSetMealNotifyView() {
        llNotifyParent.visible()
        val textView = TextView(context)
        textView.setTextColor(Color.parseColor("#666666"))
        textView.textSize = 10f
        textView.text = "您的套餐已到期，请及时续费 ！！！"
        textView.clickN {
            jumpToAccountRecharge()
        }
        viewFlipper.addView(textView)
    }

    /**
     * 检测通知提醒
     */
    private fun checkNotifyRemind() {
        activity?.let {
            LogUtils.i("checkNotifyRemind=${it.checkNotifyIsEnable()}")
            if (!it.checkNotifyIsEnable()) {
                NotifyRemindDialog.showDialog(it, it)
            }
        }
    }


    /**
     * 展示绑定校园卡账号的对话框
     */
    private fun showBindSchoolAccountDialog() {
        activity?.let {
            var result = ""
            MaterialDialog(it)
                .cancelOnTouchOutside(false)
                .cornerRadius(8f)
                .title(text = "提示")
                .message(text = "用户您好，系统检测到您使用的套餐为运营商账号，请确定您使用的运营商账号")
                .input(
                    hint = "请输入运营商账号",
                    inputType = InputType.TYPE_CLASS_PHONE,
                    waitForPositiveButton = true,
                    callback = { _, text ->
                        result = text.toString()
                    })
                .lifecycleOwner(it)
                .positiveButton(text = "确定") { dialog ->
                    dialog.dismiss()
                    viewModel.bindSchoolAccount(
                        account = mAccountModel!!.user,
                        proxy_user = result,
                        proxy_vlan = NetworkUtils.getWifiName(context),
                        apiPass = mSchoolModel?.findSecret ?: ""
                    )
                }.show {
                    val inputField = getInputField()
                    if (mAccountModel?.user?.length == 11 && mAccountModel?.user?.startsWith("1") == true) {
                        inputField.setText(mAccountModel?.user)
                        inputField.setSelection(mAccountModel?.user?.length ?: 0)
                    }
                }
        }
    }

    /**
     * 弹出选择学校的Dialog
     */
    private fun showSelectSchoolDialog() {
        activity?.let {
            if (mSchoolModelList.isNullOrEmpty()) {
                viewModel.getAllSchoolInfo()
            } else {
                val items = mSchoolModelList!!.map { schoolModel ->
                    schoolModel.name
                }
                SelectSchoolDialog.showDialog(it, it, items) { index ->
                    mSchoolModel = mSchoolModelList!![index]
                    mSchoolSelectInfoGson = Gson().toJson(mSchoolModel)
                    tvSchoolName.text = mSchoolModel?.name
                    //选择学校改变的时候发送通知
                    LiveDataBus.with(SCHOOL_MODEL_CHANGE_KEY).postValue(mSchoolModel)
                }
            }
        }
    }

    /**
     * 更新卡片上的信息
     */
    private fun updateTopCardInfo() {
        val info = buildString {
            mAccountInfoMaps.forEach {
                append("${it.key} : ${it.value}\n")
            }
        }
        idUserMessageTv.text = info
        if (mAccountModel != null) {
            val timeSpace = DateUtils.getTwoDay(
                mAccountModel!!.expiretime,
                DateUtils.dateToStr(DateUtils.getNow())
            )
            if (timeSpace < 0) {
                showNoSetMealNotifyView()
            } else {
                llNotifyParent.gone()
            }
        }
    }

    /**
     * 展示绑定账号的对话框
     */
    private fun showBindAccountDialog() {
        activity?.let {
            BindAccountDialog.showDialog(
                it,
                it,
                mAccountModel,
                callBack = { account, password ->
                    mBindAccount = account
                    mBindAccountPassword = password
                    viewModel.bindAccount(account, password)
                },
                modifyProxyUserCallBack = { proxyUser ->
                    viewModel.bindSchoolAccount(
                        account = mAccountModel!!.user,
                        proxy_user = proxyUser,
                        proxy_vlan = NetworkUtils.getWifiName(context),
                        apiPass = mSchoolModel?.findSecret ?: ""
                    )
                }
            )
        }
    }

    /**
     * 检测账号和学校是否存在
     */
    private fun checkAccountAndSchoolIsExit(): Boolean {
        if (mSchoolModel == null) {
            ToastUtils.toastShort("请先选择学校")
            showSelectSchoolDialog()
            return false
        }
        if (mAccountModel == null) {
            showBindAccountDialog()
            return false
        }
        return true
    }

    /**
     * 提示尚未购买套餐跳转到购买套餐的Dialog
     */
    private fun showNoSetMealDialog(message: String) {
        activity?.let {
            MaterialDialog(it)
                .cancelable(true)
                .cornerRadius(8f)
                .title(text = "提示")
                .message(text = message)
                .lifecycleOwner(it)
                .positiveButton(text = "确定") { dialog ->
                    dialog.dismiss()
                    jumpToAccountRecharge()
                }.negativeButton(text = "取消") { dialog ->
                    dialog.dismiss()
                }.show()
        }
    }

    /**
     * 当账号的学校和选择的学校不一致的时候提示用户统一设置学校
     *
     */
    private fun showSchoolBindAccountTipsDialog(content: String) {
        activity?.let {
            SchoolBindAccountTipsDialog.showDialog(it, it, content) {
                val items = mSchoolModelList!!.map { schoolModel ->
                    schoolModel.name
                }
                SelectSchoolDialog.showDialog(it, it, items) { index ->
                    mSchoolModel = mSchoolModelList!![index]
                    mSchoolSelectInfoGson = Gson().toJson(mSchoolModel)
                    tvSchoolName.text = mSchoolModel?.name
                }
            }
        }
    }


    /**
     * 跳转到账号充值
     */
    private fun jumpToAccountRecharge() {
        if (checkAccountAndSchoolIsExit()) {
            isModifyPackage = true
            startKtxActivityForResult<WebActivity>(
                requestCode = 0x01, values = listOf(
                    Pair(
                        "url",
                        "http://yonghu.guangzhida.cn/lfradius/home.php?a=userlogin&c=login"
                    ),
                    Pair("type", "AccountRecharge"),
                    Pair(
                        "params",
                        "username=${mAccountModel?.user}&password=${mAccountModel?.pass}"
                    )
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isModifyPackage) {
            isModifyPackage = false
            mAccountModel?.let { accountModel ->
                viewModel.getAccountInfo(accountModel.user)
            }
        }

    }


}