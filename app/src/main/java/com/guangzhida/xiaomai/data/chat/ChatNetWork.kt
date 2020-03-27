package com.guangzhida.xiaomai.data.chat

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.ChatUserModel
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field

/**
 * 聊天的网络请求数据
 */
class ChatNetWork {
    private val mService by lazy { RetrofitManager.getInstance().create(ChatService::class.java) }

    /**
     * 获取好友列表信息
     */
    suspend fun getFriendList(): BaseResult<List<ChatUserModel>> {
        return mService.getFriendList()
    }

    /**
     * 同意或者拒绝好友申请
     */
    suspend fun agreeAddFriend(friendId: String, isAgree: String): BaseResult<String> {
        return mService.agreeAddFriend(friendId, isAgree)
    }

    /**
     * 发送添加好友申请
     */
    suspend fun sendAddFriends(friendId: String, message: String): BaseResult<String> {
        return mService.sendAddFriends(friendId, message)
    }

    /**
     * 删除好友
     */
    suspend fun removeFriend(friendId: String): BaseResult<String> {
        return mService.removeFriend(friendId)
    }

    /**
     * 发送文本消息
     */
    suspend fun sendTextMsg(friendId: String, context: String): BaseResult<String> {
        return mService.sendTextMsg(friendId, context)
    }

    /**
     * 发送图片或者语音
     */
//    suspend fun sendPicOrVoiceMsg(): BaseResult<String> {
//       return mService.sendPicOrVoiceMsg(RequestBody.create())
//    }
    /**
     * 根据昵称和手机号获取用户信息
     */
    suspend fun getUserInfoByNickNameOrPhone(nickName: String = "",phone: String = ""): BaseResult<List<ChatUserModel>>{
        return mService.getUserInfoByNickNameOrPhone(nickName,phone)
    }
    companion object {
        @Volatile
        private var netWork: ChatNetWork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: ChatNetWork().also { netWork = it }
            }
    }

}