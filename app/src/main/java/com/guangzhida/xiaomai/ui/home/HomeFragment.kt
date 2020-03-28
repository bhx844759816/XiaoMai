package com.guangzhida.xiaomai.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.*
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.dialog.BindAccountDialog
import com.guangzhida.xiaomai.dialog.NetworkCheckDialog
import com.guangzhida.xiaomai.dialog.QueryBalanceDialog
import com.guangzhida.xiaomai.event.netChangeLiveData
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.receiver.WifiStateManager
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.home.viewmodel.HomeViewModel
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.utils.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_home_center_grid.*
import pub.devrel.easypermissions.EasyPermissions
import java.lang.StringBuilder

/**
 * 首页校园网页面
 */
class HomeFragment : BaseFragment<HomeViewModel>(), EasyPermissions.PermissionCallbacks {
    private val mAccountInfoMaps = mutableMapOf<String, String>()
    private val mNetworkCheckSb = StringBuilder()
    private var isNetError = false
    private var mAccountModel: AccountModel? = null
    private var mSchoolModel: SchoolModel? = null

    private lateinit var schoolName: String

    override fun layoutId(): Int = R.layout.fragment_home

    override fun initView(savedInstanceState: Bundle?) {
        for (index in 0..3) {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_home_loop_ad, null)
            idViewFlipper.addView(view)
        }
        idViewFlipper.flipInterval = 2000
        idViewFlipper.startFlipping()
        initListener()
        registerLiveData()
        requestPermission()
        //获取账户信息
        val account = SPUtils.get(context, BIND_ACCOUNT_CARD_KEY, "") as String
        if (account.isNotEmpty()) {
            viewModel.getAccountInfo(account)
        }
        //获取选中的学校
        schoolName = SPUtils.get(context, SEARCH_SCHOOL_KEY, "") as String
        if (schoolName.isNotEmpty()) {
            viewModel.getSchoolInfo(schoolName)
        }
    }

    private fun initData() {
        val isWifiConnect = NetworkUtils.isWifiConnected(context)
        val mWifiName = NetworkUtils.getWifiName(activity)
        val mIpAddress = NetworkUtils.getIPAddress(true)
        mAccountInfoMaps["wifi名称"] = ""
        mAccountInfoMaps["所在院校"] =
            SPUtils.get(context, SEARCH_SCHOOL_KEY, "") as String
        mAccountInfoMaps["IP地址"] = ""
        mAccountInfoMaps["账号"] = ""
        mAccountInfoMaps["使用套餐"] = ""
        mAccountInfoMaps["到期时间"] = ""
        if (isWifiConnect) {
            mAccountInfoMaps["wifi名称"] = mWifiName
            mAccountInfoMaps["IP地址"] = mIpAddress
        }
        updateUserInfo()
    }

    private fun requestPermission() {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            initData()
        } else {
            EasyPermissions.requestPermissions(
                this, "应用程序需要读取wifi名称需要定位权限",
                0x01, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun initListener() {
        //绑定账号
        idBindAccount.setOnClickListener {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请选择学校")
                jumpToSelectSchool()
            } else {
                //获取账号信息
                activity?.let {
                    BindAccountDialog.showDialog(it, it) { account, password ->
                        viewModel.bindAccount(account, password)
                    }
                }
            }

        }
        //用户注册
        llRegister.setOnClickListener {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请先选择学校")
                jumpToSelectSchool()
            } else {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra("url", mSchoolModel!!.regiestNewUser)
                intent.putExtra("type", "AccountRegister")
                startActivity(intent)
            }
        }
        //连接wifi
        idConnectWifiBtn.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }
        //点击选择学校
        ivSearchSchool.setOnClickListener {
            jumpToSelectSchool()
        }
        //消息中心
        ivMessageNotify.setOnClickListener {
            startActivity(Intent(context, MessageCenterActivity::class.java))
        }
        //查询余额
        llQueryBalance.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                activity?.let {
                    QueryBalanceDialog.showDialog(it, it, mAccountModel!!)
                }
            }
        }
        //账户充值
        llAccountRecharge.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(
                    "url",
                    "http://yonghu.guangzhida.cn/lfradius/home.php?a=userlogin&c=login"
                )
                intent.putExtra("type", "AccountRecharge")
                intent.putExtra(
                    "params",
                    "username=${mAccountModel?.user}&password=${mAccountModel?.pass}"
                )
                startActivity(intent)
            }
        }
        //套餐修改
        llAccountPackageModify.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(
                    "url",
                    "http://yonghu.guangzhida.cn/lfradius/home.php?a=userlogin&c=login"
                )
                intent.putExtra("type", "AccountRecharge")
                intent.putExtra(
                    "params",
                    "username=${mAccountModel?.user}&password=${mAccountModel?.pass}"
                )
                startActivity(intent)
            }

        }
        //关于密码
        aboutPassword.setOnClickListener {
            if (checkAccountAndSchoolIsExit()) {
                val intent = Intent(context, WebActivity::class.java)
                intent.putExtra(
                    "url",
                    mSchoolModel?.findPwd
                )
                intent.putExtra("type", "ForgetPassword")
                startActivity(intent)
            }
        }
        //我的客服
        llService.setOnClickListener {
            if (BaseApplication.instance().userModel == null) {
                startActivity(Intent(context, LoginActivity::class.java))
            } else {

            }
        }
        //网络诊断
        llNetworkDiagnosis.setOnClickListener {
            if (NetworkUtils.isConnected(context)) {
                mNetworkCheckSb.clear()
                mNetworkCheckSb.append("提示：当前数据连接为")
                    .append(NetworkUtils.getCurrentNetworkType(context))
                    .append("\n")
                    .append("接入点MAC:")
                    .append(NetworkUtils.getMac())
                    .append("\n")
                activity?.let {
                    isNetError = false
                    NetworkCheckDialog.showDialog(it, it, mNetworkCheckSb.toString()) {
                        viewModel.cancelNetWorkCheck()
                    }
                    viewModel.doNetWorkCheck()
                }
            } else {
                ToastUtils.toastShort("请先连接网络")
            }
        }
        //一键认证
        tvVerify.setOnClickListener {
            if (!NetworkUtils.isWifiConnected(context)) {
                ToastUtils.toastShort("请先连接wifi")
                return@setOnClickListener
            }
            if (checkAccountAndSchoolIsExit()) {
                val url = mSchoolModel!!.keyAuthentication
                val params = mapOf(
                    "wlanuserip" to NetworkUtils.getIPAddress(true),
                    "wlanapmac" to NetworkUtils.getMac(),
                    "username" to mAccountModel?.user,
                    "password" to mAccountModel?.pass,
                    "line" to "4"
                )
                LogUtils.i(params.toString())
                viewModel.doAccountVerify(url, params)
            }
        }
        //退出认证
        tvQuitVerify.setOnClickListener {
            if (!NetworkUtils.isWifiConnected(context)) {
                ToastUtils.toastShort("请先连接wifi")
                return@setOnClickListener
            }
            if (checkAccountAndSchoolIsExit()) {
                val url = mSchoolModel!!.logoutLogin
                val params = mapOf(
                    "ip" to NetworkUtils.getIPAddress(true),
                    "mac" to NetworkUtils.getConnectedWifiMacAddress(context)
                )
                viewModel.quitAccountVerify(url, params = params)
            }
        }


//        wlanuserip=192.168.11.248,
//        wlanapmac=8E:5F:12:02:6B:B2,
//        username=19137629693,
//        password=123456,
//        line=4
    }

    /**
     * 权限被拒绝
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        requestPermission()
    }

    /**
     *权限申请通过
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        requestPermission()
    }

    /**
     * 权限回调结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SELECT_SCHOOL -> {
                    mAccountInfoMaps["所在院校"] = SPUtils.get(context, SEARCH_SCHOOL_KEY, "") as String
                    mAccountInfoMaps["所在院校"]?.let {
                        viewModel.getSchoolInfo(it)
                    }
                    updateUserInfo()
                }
            }
        }
    }

    /**
     * 注册观察者
     */
    private fun registerLiveData() {
        //获取账号信息
        viewModel.mAccountModelData.observe(this, Observer {
            SPUtils.put(context, BIND_ACCOUNT_CARD_KEY, it.user)
            mAccountModel = it
            mAccountInfoMaps["账号"] = it.user
            mAccountInfoMaps["使用套餐"] = it.servername
            mAccountInfoMaps["到期时间"] = it.expiretime
            mAccountInfoMaps["所在院校"] = it.name
            updateUserInfo()
        })
        //获取到学校信息
        viewModel.mSchoolModelData.observe(this, Observer {
            LogUtils.i("SchoolModel=$it")
            mSchoolModel = it
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
                }
            }
        })
        //网络检测结果
        viewModel.netWorkCheckResult.observe(this, Observer {
            val str = when (it.first) {
                "内网" -> {
                    buildString {
                        append("内网ping值:")
                        append(it.second.averageDelay)
                        append("\n")
                        append("丢包率:")
                        append(it.second.lossPackageRate)
                        append("\n")
                        if (!it.second.success) {
                            isNetError = true
                            append("内网网线or环路or交换机---疑似问题\n")
                        }
                    }
                }
                "外网" -> {
                    buildString {
                        append("外网ping值:")
                        append(it.second.averageDelay)
                        append("\n")
                        append("丢包率:")
                        append(it.second.lossPackageRate)
                        append("\n")
                        if (!it.second.success) {
                            isNetError = true
                            append("路由器和wan口线路---疑似问题\n")
                        }
                    }
                }
                "公网" -> {
                    if (!it.second.success) {
                        isNetError = true
                        "路由器和wan口线路---疑似问题\n"
                    } else {
                        if (isNetError) {
                            "网络异常，请联系在线客服\n"
                        } else {
                            "检测网络正常\n"
                        }
                    }
                }
                else -> ""
            }
            mNetworkCheckSb.append(str)
            NetworkCheckDialog.changeDialogMessage(mNetworkCheckSb.toString())
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
            updateUserInfo()
        })
    }

    /**
     * 更新卡片上的信息
     */
    private fun updateUserInfo() {
        val info = buildString {
            mAccountInfoMaps.forEach {
                append("${it.key} : ${it.value}\n")
            }
        }
        idUserMessageTv.text = info
    }

    private fun bindAccount() {
        activity?.let {
            BindAccountDialog.showDialog(it, it) { account, password ->
                viewModel.bindAccount(account, password)
            }
        }
    }

    /**
     * 跳转到选择学校
     */
    private fun jumpToSelectSchool() {
        startActivityForResult(
            Intent(context, SearchSchoolActivity::class.java),
            REQUEST_CODE_SELECT_SCHOOL
        )
    }

    /**
     * 检测账号和学校是否存在
     */
    private fun checkAccountAndSchoolIsExit(): Boolean {
        if (mSchoolModel == null) {
            ToastUtils.toastShort("请先选择学校")
            jumpToSelectSchool()
            return false
        }
        if (mAccountModel == null) {
            ToastUtils.toastShort("请先绑定账号")
            bindAccount()
            return false
        }
        return true
    }

    companion object {
        const val REQUEST_CODE_SELECT_SCHOOL = 0x01
    }

}