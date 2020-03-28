package com.guangzhida.xiaomai

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.multidex.MultiDexApplication
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.SPUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMOptions


class BaseApplication : MultiDexApplication() {
    //存储用户信息
    var userModel: UserModel.Data? = null //存储用户信息
    var mToken: String? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        mToken = SPUtils.get(this, USER_TOKEN_KEY, "") as String
        LogUtils.init()
        ToastUtils.init(this)
        initChat()
    }

    /**
     * 初始化环信SDK
     */
    private fun initChat() {
        val pid = Process.myPid()
        val processAppName = getAppName(pid)
        if (processAppName == null || !processAppName.equals(
                packageName,
                ignoreCase = true
            )
        ) {
            return
        }
        val options = EMOptions()
        // 设置自动登录
        options.autoLogin = true
        // 设置是否需要发送已读回执
        options.requireAck = true
        // 设置是否需要发送回执，
        options.requireDeliveryAck = true
        // 设置是否根据服务器时间排序，默认是true
        options.isSortMessageByServerTime = false
        // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
        options.acceptInvitationAlways = false
        // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
        options.isAutoAcceptGroupInvitation = false
        // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
        options.isDeleteMessagesAsExitGroup = false
        // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
        options.allowChatroomOwnerLeave(true)
        // 设置google GCM推送id，国内可以不用设置
        // options.setGCMNumber(MLConstants.ML_GCM_NUMBER);
        // 设置集成小米推送的appid和appkey
        // options.setMipushConfig(MLConstants.ML_MI_APP_ID, MLConstants.ML_MI_APP_KEY);
        EMClient.getInstance().init(this, options)
    }


    private fun getAppName(pID: Int): String? {
        var processName: String? = null
        val am =
            this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l: List<*> = am.runningAppProcesses
        val i = l.iterator()
        val pm = this.packageManager
        while (i.hasNext()) {
            val info = i.next() as RunningAppProcessInfo
            try {
                if (info.pid == pID) {
                    processName = info.processName
                    return processName
                }
            } catch (e: Exception) {
            }
        }
        return processName
    }


    /**
     * 获取MyApplication得单例
     */
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: BaseApplication? = null

        fun instance() = instance!!
    }
}