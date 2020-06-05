package com.guangzhida.xiaomai.chat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.utils.LogUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage

/**
 * 聊天的服务
 *
 * 用于监听 消息得接收 好友请求 展示通知
 *
 */
class ChatService : Service() {
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
                    ChatHelper.mUserDao?.let { userDao ->
                        val user = userDao.queryUserByUserName(message.from)
                        if (user != null && message.isUnread) {
                            ChatHelper.notifier?.notify(
                                message,
                                ChatHelper.getUnReadMessageCount()
                            )
                            ChatHelper.notifier?.vibrateAndPlayTone()
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        LogUtils.i("ChatService onCreate")
//        EMClient.getInstance().chatManager().addMessageListener(myEMMessageListener)
    }

    var num = 0
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        LogUtils.i("ChatService onStartCommand")
//        Thread {
//            while (true){
//                try {
//                    Thread.sleep(1000)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//                num++
//                LogUtils.i("ChatService=$num")
//            }
//        }.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.i("ChatService onDestroy")
    }
}