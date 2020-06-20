package com.guangzhida.xiaomai.chat

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Process
import androidx.work.*
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.NETWORK_CHECK_RESULT_FILENAME
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey.IM_CONNECT_SERVER_KEY
import com.guangzhida.xiaomai.event.LiveDataBusKey.IM_DISCONNECT_SERVER_KEY
import com.guangzhida.xiaomai.event.LiveDataBusKey.IM_KICKED_BY_OTHER_DEVICE
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.ktxlibrary.ext.readTxtFile
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.task.UpdateConversationTask
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMContactListener
import com.hyphenate.EMError
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.*
import java.util.concurrent.Executors


/**
 * 聊天的帮助类
 */
object ChatHelper {
    var notifier: NotificationUtils? = null
    lateinit var appContext: Context
    val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    private val mExecutor = Executors.newSingleThreadExecutor()
    //
    private val mCheckResultSavePath by lazy {
        BaseApplication.instance().getExternalFilesDir("network")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/network"
    }
    /**
     * 联系人改变的监听
     */
    private val myContactListener = object : EMContactListener {
        override fun onContactInvited(username: String, reason: String) {
            LogUtils.i("EaseUiHelper 收到好友邀请$username")
            //收到好友邀请
            val inviteMessageList = mInviteMessageDao?.queryAll()
            inviteMessageList?.forEach {
                if (it.from == username) {
                    mInviteMessageDao?.delete(it)
                }
            }
            val inviteMessage = InviteMessageEntity(
                from = username,
                time = System.currentTimeMillis(),
                reason = reason,
                userName = BaseApplication.instance().mUserModel!!.username,
                state = 0
            )
            mInviteMessageDao?.insert(inviteMessage)
            notifier?.vibrateAndPlayTone()
            //接收到好友邀请的时候发送消息

            // TODO 需要发送好友邀请，主页面  联系人界面 互动界面需要监听 改变显示的小红点和好友信息
            messageCountChangeLiveData.postValue(true)
        }

        override fun onContactDeleted(username: String?) {
            try {
                username?.let {
                    val userEntity = mUserDao?.queryUserByUserName(it)
                    if (userEntity != null) {
                        mUserDao?.delete(userEntity)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        override fun onFriendRequestAccepted(username: String) {
            try {
                val inviteMessage = mInviteMessageDao?.queryInviteMessageByFrom(username)
                if (inviteMessage != null) {
                    inviteMessage.state = 2
                    mInviteMessageDao?.update(inviteMessage)
                }
            } catch (e: Throwable) {

            }

        }

        override fun onContactAdded(username: String?) {
        }

        override fun onFriendRequestDeclined(username: String?) {
            //好友请求被拒绝
        }
    }

    /**
     * 消息接收的监听
     */
    private val myEMMessageListener = object : EMMessageListener {
        override fun onMessageRecalled(messages: MutableList<EMMessage>?) {

        }

        override fun onMessageChanged(message: EMMessage?, change: Any?) {
        }

        override fun onCmdMessageReceived(messages: MutableList<EMMessage>?) {
            LogUtils.i("chatHelper onCmdMessageReceived")
            //接收到透传消息
            messages?.forEach {
                val emMessageBody = it.body
                if (emMessageBody is EMCmdMessageBody) {
                    //获取用户当前的网络状况
                    if (emMessageBody.action() == "getUserNetworkState") {
                        val serverName = it.getStringAttribute("serverName")
                        replyServerNetWorkStatus(serverName)
                    }
                }
            }
        }

        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
            try {
                LogUtils.i("onMessageReceived")
                messages?.let {
                    it.forEach { message ->
                        //启动Task更新本地的会话列表
                        updateConversationEntity(message.from)
                        //弹出消息对话框
                        notifier?.notify(message, getUnReadMessageCount())
                        notifier?.vibrateAndPlayTone()
                        //更新未读消息个数
                        messageCountChangeLiveData.postValue(true)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {
        }
    }

    /**
     * 与服务器的连接监听
     */
    private val mConnectListener = object : EMConnectionListener {
        override fun onConnected() {
            LiveDataBus.with(IM_CONNECT_SERVER_KEY).postValue(true)
        }

        override fun onDisconnected(error: Int) {
            LogUtils.i("IM onDisconnected errorCode =$error")
            if (error == EMError.USER_LOGIN_ANOTHER_DEVICE || error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
                //其他设备登录账号
                LiveDataBus.with(IM_KICKED_BY_OTHER_DEVICE).postValue(true)
            }
            //断开连接
            LiveDataBus.with(IM_DISCONNECT_SERVER_KEY).postValue(true)
        }
    }

    /**
     * 初始化环信
     */
    fun init(context: Context, packageName: String) {
        val pid = Process.myPid()
        val processAppName =
            getAppName(context, pid)
        if (processAppName == null || !processAppName.equals(
                packageName,
                ignoreCase = true
            )
        ) {
            return
        }
        val options: EMOptions = initOption()
        EMClient.getInstance().init(context, options)
        appContext = context
        notifier = NotificationUtils(context)
        notifier?.setNotificationInfoProvider(object :
            NotificationUtils.EaseNotificationInfoProvider {
            override fun getLaunchIntent(message: EMMessage?): Intent {
                val intent = Intent(appContext, ChatMessageActivity::class.java)
                intent.putExtra("userName", message?.from)
                return intent
            }

            override fun getSmallIcon(message: EMMessage?): Int {
                return 0
            }

            override fun getTitle(message: EMMessage?): String? {
                return null
            }

            override fun getLatestText(
                message: EMMessage?,
                fromUsersNum: Int,
                messageNum: Int
            ): String? {
                return null
            }

            override fun getDisplayedText(message: EMMessage?): String {
                message?.let {
                    var ticker: String =
                        getMessageDigest(
                            it
                        )
                    if (it.type == EMMessage.Type.TXT) {
                        ticker = ticker.replace("\\[.{2,3}\\]".toRegex(), "[表情]")
                    }
                    val userEntity =
                        AppDatabase.invoke(appContext).userDao()?.queryUserByUserName(it.from)
                    return if (userEntity != null) {
                        userEntity.nickName + " : " + ticker
                    } else {
                        ticker
                    }
                }
                return ""
            }

        })
        setGlobalListeners()
    }

    /**
     * 取消通知栏
     */
    fun cancelNotifyMessage() {
        notifier?.reset()
    }

    /**
     * 获取消息类型
     */
    private fun getMessageDigest(
        message: EMMessage
    ): String {
        return when (message.type) {
            EMMessage.Type.LOCATION -> "[地址]"
            EMMessage.Type.IMAGE -> "[图片]"
            EMMessage.Type.VOICE -> "[语音]"
            EMMessage.Type.VIDEO -> "[视频]"
            EMMessage.Type.FILE -> "[文件]"
            EMMessage.Type.TXT -> {
                val txtBody = message.body as EMTextMessageBody
                txtBody.message
            }
            else -> {
                return ""
            }
        }
    }

    /**
     * 设置全局监听事件
     */
    private fun setGlobalListeners() {
        EMClient.getInstance().contactManager().setContactListener(myContactListener)
        EMClient.getInstance().chatManager().addMessageListener(myEMMessageListener)
        EMClient.getInstance().addConnectionListener(mConnectListener)
    }

    /**
     * 通过接收到的消息保存到本地的会话中去
     */
    fun updateConversationEntity(userName: String) {
        val constraints: Constraints = Constraints.Builder()
            .build()
        val data: Data = Data.Builder().putString(
            "userName",
            userName
        ).build()
        val updateConversationTask =
            OneTimeWorkRequest.Builder(UpdateConversationTask::class.java)
                .addTag("UpdateConversationTask")
                .setInputData(data)
                .setConstraints(constraints) //设置触发条件
                .build()
        WorkManager.getInstance(BaseApplication.instance().applicationContext)
            .enqueue(updateConversationTask)
    }

    /**
     * 获取未读消息得个数
     */
    fun getUnReadMessageCount(): Int {
        return EMClient.getInstance().chatManager().unreadMessageCount
    }


    /**
     * 回复客服网络状况
     */
    private fun replyServerNetWorkStatus(serverName: String) {
        mExecutor.submit {
            val content = readTxtFile(mCheckResultSavePath, NETWORK_CHECK_RESULT_FILENAME)
            LogUtils.i("用户网络状况=$content")
            val cmdMessage = EMMessage.createSendMessage(EMMessage.Type.CMD)
            val cmdBody = EMCmdMessageBody("replyUserNetworkState")
            cmdMessage.addBody(cmdBody)
            cmdMessage.to = serverName
            cmdMessage.from = BaseApplication.instance().mUserModel?.username
            cmdMessage.setAttribute("userName", BaseApplication.instance().mUserModel?.username)
            cmdMessage.setAttribute("content", content ?: "")
            EMClient.getInstance().chatManager().sendMessage(cmdMessage)
        }
    }

    private fun getAppName(context: Context, pID: Int): String? {
        var processName: String? = null
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l: List<*> = am.runningAppProcesses
        val i = l.iterator()
        while (i.hasNext()) {
            val info = i.next() as ActivityManager.RunningAppProcessInfo
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

    private fun initOption(): EMOptions {
        return EMOptions().apply {
            // 设置自动登录
            autoLogin = true
            // 设置是否需要发送已读回执
            requireAck = true
            // 设置是否需要发送回执，
            requireDeliveryAck = true
            // 设置是否根据服务器时间排序，默认是true
            isSortMessageByServerTime = false
            // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
            acceptInvitationAlways = false
            // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
            isAutoAcceptGroupInvitation = false
            // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
            isDeleteMessagesAsExitGroup = false
            // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
            allowChatroomOwnerLeave(true)
        }
    }
}