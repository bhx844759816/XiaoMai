package com.guangzhida.xiaomai.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SEARCH_SCHOOL_KEY
import com.guangzhida.xiaomai.SEARCH_SCHOOL_USER_REGISTER_KEY
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.dialog.BindAccountDialog
import com.guangzhida.xiaomai.dialog.NetworkCheckDialog
import com.guangzhida.xiaomai.dialog.QueryBalanceDialog
import com.guangzhida.xiaomai.event.netChangeLiveData
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
        viewModel.getAccountInfo("15801106869")
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
            activity?.let {
                BindAccountDialog.showDialog(it, it) { account, password ->
                    viewModel.bindAccount(account, password)
                }
            }
        }
        //用户注册
        llRegister.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
//            val url = SPUtils.get(context, SEARCH_SCHOOL_USER_REGISTER_KEY, "") as String
//            if (url.isEmpty()) {
//                ToastUtils.toastShort("请先选择学校")
//                startActivityForResult(
//                    Intent(context, SearchSchoolActivity::class.java),
//                    REQUEST_CODE_SELECT_SCHOOL
//                )
//            } else {
//                val intent = Intent(context, WebActivity::class.java)
//                intent.putExtra("url", url)
//                startActivity(intent)
//            }
        }
        idConnectWifiBtn.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }
        ivSearchSchool.setOnClickListener {
            startActivityForResult(
                Intent(context, SearchSchoolActivity::class.java),
                REQUEST_CODE_SELECT_SCHOOL
            )
        }
        //消息中心
        ivMessageNotify.setOnClickListener {
            startActivity(Intent(context, MessageCenterActivity::class.java))
        }
        //查询余额
        llQueryBalance.setOnClickListener {
            activity?.let {
                QueryBalanceDialog.showDialog(it, it)
            }
        }
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
            mAccountInfoMaps["账号"] = it.user
            mAccountInfoMaps["使用套餐"] = it.servername
            mAccountInfoMaps["到期时间"] = it.expiretime
            mAccountInfoMaps["所在院校"] = it.name
            updateUserInfo()
        })
        viewModel.bindResult.observe(this, Observer {
            if (it) {
                ToastUtils.toastShort("绑定成功")
                BindAccountDialog.dismissDialog()
            } else {
                ToastUtils.toastShort("绑定失败")
            }
        })
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


    companion object {
        const val REQUEST_CODE_SELECT_SCHOOL = 0x01
    }

}