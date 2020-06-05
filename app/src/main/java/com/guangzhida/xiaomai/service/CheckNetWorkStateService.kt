package com.guangzhida.xiaomai.service

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.NETWORK_CHECK_RESULT_FILENAME
import com.guangzhida.xiaomai.ktxlibrary.ext.writeTxtFile
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.PingResultModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.utils.*
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCmdMessageBody
import com.hyphenate.chat.EMMessage
import java.util.concurrent.Executors

class CheckNetWorkStateService : Service() {
    private val mExecutor = Executors.newSingleThreadExecutor()
    private var isChecking = false
    //检测结果保存的文件 -> 上传到云端 ->
    private val mCheckResultSavePath by lazy {
        getExternalFilesDir("network")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/network"
    }
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mAccountModel by lazy {
        Gson().fromJson(mSchoolAccountInfoGson, AccountModel::class.java)
    }
    //选择的学校信息
    private val mSelectSchoolModel by lazy {
        Gson().fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serverName = intent?.getStringExtra("serverName") //校麦服务端的环信ID
        val userName = intent?.getStringExtra("userName") //发送者的环信ID
        if (userName != null && serverName != null) {
            startCheck(userName, serverName)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getUserAccountInfo(): String {
        var isExpire = "未绑定账号"
        if (mAccountModel != null) {
            val timeSpace = DateUtils.getTwoDay(
                mAccountModel!!.expiretime,
                DateUtils.dateToStr(DateUtils.getNow())
            )
            isExpire = if (timeSpace < 0) {
                "已到期"
            } else {
                "未到期"
            }
        }
        return buildString {
            append("      ----用户网络状况---- \n")
            append("选择学校:")
            append(mSelectSchoolModel?.name ?: "未选择学校")
            append("\n")
            append("所属区域:")
            append(mAccountModel?.name ?: "未选择所属区域")
            append("\n\n")
            append("账号:")
            append(mAccountModel?.user ?: "未绑定账号")
            append("\n")
            append("密码:")
            append(mAccountModel?.pass ?: "未绑定账号")
            append("\n")
            append("账号类型:")
            append(mAccountModel?.servername ?: "未绑定账号")
            append("\n")
            append("到期时间:")
            append(mAccountModel?.expiretime ?: "未绑定账号")
            append("\n")
            append("是否到期:")
            append(isExpire)
            append("\n\n")
            append("wifi名称:")
            append(NetworkUtils.getWifiName(this@CheckNetWorkStateService.applicationContext))
            append("\n")
            append("IP地址:")
            append(NetworkUtils.getIPAddress(true))
            append("\n")
            append("wifi强度:")
            append(
                NetworkUtils.getWifiRssi(this@CheckNetWorkStateService.applicationContext)
                    ?: "未检测到wifi信号强度"
            )
            append("\n")
        }
    }

    /**
     * 开始检测
     */
    private fun startCheck(userName: String, serverName: String) {
        mExecutor.submit {
            isChecking = true
            val ip = NetworkUtils.getIPAddress(true);
            val checkResultSb = StringBuilder()
            checkResultSb.append(getUserAccountInfo())
            checkResultSb.append("网络检测\n")
            var gatewayResult: PingResultModel? = null
            if (NetworkUtils.isWifiConnected(this.applicationContext)) {
                //网关ip
                val gatewayIp = ip.substring(0, ip.lastIndexOf(".")).plus(".1")
                gatewayResult = NetworkDiagnosisManager.executeCmd2(gatewayIp, 3)
                val pingGatewayResult = buildString {
                    append("      本机 - 网关")
                    append("      \n")
                    append("      ping值:")
                    append(gatewayResult.averageDelay)
                    append("      \n")
                    append("      丢包率:")
                    append(gatewayResult.lossPackageRate)
                    append("\n")
                }
                checkResultSb.append(pingGatewayResult)
            }
            val extraNetResultModel = NetworkDiagnosisManager.executeCmd2("122.51.167.92", 3)
            val extraNetResult = buildString {
                append("      本机 - 服务器")
                append("      \n")
                append("      ping值:")
                append(extraNetResultModel.averageDelay)
                append("      \n")
                append("      丢包率:")
                append(extraNetResultModel.lossPackageRate)
            }
            checkResultSb.append(extraNetResult)
            checkResultSb.append("\n")
            val isCheckSuccess = if (gatewayResult == null) {
                extraNetResultModel.success
            } else {
                extraNetResultModel.success && gatewayResult.success
            }
            if (isCheckSuccess) {
                checkResultSb.append("网络检测正常")
            } else {
                checkResultSb.append("网络检测异常")
            }
            //将当时的检测结果保存到本地
            writeTxtFile(
                checkResultSb.toString(),
                mCheckResultSavePath,
                NETWORK_CHECK_RESULT_FILENAME,
                false
            )
            LogUtils.i("写入文件成功")
            sendCmdMessage(userName, serverName, checkResultSb.toString())
        }
    }

    /**
     * 发送透传消息给校麦服务端
     */
    private fun sendCmdMessage(userName: String, serverName: String, content: String) {
        val cmdMessage = EMMessage.createSendMessage(EMMessage.Type.CMD)
        val cmdBody = EMCmdMessageBody("replyUserNetworkState")
        cmdMessage.addBody(cmdBody)
        cmdMessage.to = serverName
        cmdMessage.from = userName
        cmdMessage.setAttribute("userName", userName)
        cmdMessage.setAttribute("content", content)
        EMClient.getInstance().chatManager().sendMessage(cmdMessage)
        isChecking = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mExecutor.shutdown()
    }
}