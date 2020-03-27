package com.guangzhida.xiaomai.data.chat

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.ChatUserModel

/**
 * 聊天的数据请求接口
 */
class ChatRepository(netWork: ChatNetWork) {
    private val mNetWork = netWork


    suspend fun getFriendList(): BaseResult<List<ChatUserModel>> {
        return mNetWork.getFriendList()
    }

    suspend fun agreeAddFriend(friendId: String, isAgree: String): BaseResult<String> {
        return mNetWork.agreeAddFriend(friendId, isAgree)
    }

    suspend fun sendAddFriends(friendId: String, message: String): BaseResult<String> {
        return mNetWork.sendAddFriends(friendId, message)
    }

    /**
     * 删除好友
     */
    suspend fun removeFriend(friendId: String): BaseResult<String> {
        return mNetWork.removeFriend(friendId)
    }

    /**
     * 发送文本消息
     */
    suspend fun sendTextMsg(friendId: String, context: String): BaseResult<String> {
        return mNetWork.sendTextMsg(friendId, context)
    }

    /**
     * 根据昵称和手机号获取用户信息
     */
    suspend fun getUserInfoByNickNameOrPhone(
        nickName: String = "",
        phone: String = ""
    ): BaseResult<List<ChatUserModel>> {
        return mNetWork.getUserInfoByNickNameOrPhone(nickName, phone)
    }

    companion object {

        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(netWork: ChatNetWork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: ChatRepository(netWork).also { INSTANCE = it }
            }
    }
}