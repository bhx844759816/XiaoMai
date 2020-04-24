package com.guangzhida.xiaomai

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Process
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import com.guangzhida.xiaomai.utils.EaseNotifier
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMContactListener
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMOptions
import com.hyphenate.chat.EMTextMessageBody


object EaseUiHelper {
    var notifier: EaseNotifier? = null
    lateinit var appContext: Context
    val mInviteMessageDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).inviteMessageDao()
    }
    val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
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
        }

        override fun onContactDeleted(username: String?) {
        }

        override fun onFriendRequestAccepted(username: String) {
            val inviteMessage = mInviteMessageDao?.queryInviteMessageByFrom(username)
            if (inviteMessage != null) {
                inviteMessage.state = 2
                mInviteMessageDao?.update(inviteMessage)
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

        }

        override fun onMessageReceived(messages: MutableList<EMMessage>?) {
            LogUtils.i("onMessageReceived")
            messages?.let {
                it.forEach { message ->
                    //查询本地是否有这个好友如果有提醒
                    mUserDao?.let { userDao ->
                        val user = userDao.queryUserByUserName(message.from)
                        if (user != null && message.isUnread) {
                            notifier?.notify(message, getUnReadMessageCount())
                            notifier?.vibrateAndPlayTone()
                            messageCountChangeLiveData.postValue(true)
                        } else {
                            EMClient.getInstance().chatManager()
                                .getConversation(BaseApplication.instance().mUserModel?.username)
                                .markMessageAsRead(message.msgId)
                        }
                    }
                }
            }
        }

        override fun onMessageDelivered(messages: MutableList<EMMessage>?) {
        }

        override fun onMessageRead(messages: MutableList<EMMessage>?) {
        }
    }

    /**
     * 初始化环信
     */
    fun init(context: Context, packageName: String) {
        val pid = Process.myPid()
        val processAppName = getAppName(context, pid)
        if (processAppName == null || !processAppName.equals(
                packageName,
                ignoreCase = true
            )
        ) {
            return
        }
        val options: EMOptions = initOption(context)
        EMClient.getInstance().init(context, options)
        appContext = context
        notifier = EaseNotifier(context)
        notifier?.setNotificationInfoProvider(object : EaseNotifier.EaseNotificationInfoProvider {
            override fun getLaunchIntent(message: EMMessage?): Intent {
                // you can set what activity you want display when user click the notification
                val intent =
                    Intent(appContext, ChatMessageActivity::class.java)
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
                    var ticker: String = getMessageDigest(it)
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
        EMClient.getInstance().contactManager()
            .setContactListener(myContactListener)
        EMClient.getInstance().chatManager().addMessageListener(myEMMessageListener)
    }

    /**
     * 获取未读消息得个数
     */
    fun getUnReadMessageCount(): Int {
        return EMClient.getInstance().chatManager().unreadMessageCount
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

    private fun initOption(context: Context): EMOptions {
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