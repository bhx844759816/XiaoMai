package com.guangzhida.xiaomai.data.chat

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.ChatUserModel
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 聊天的请求接口
 */
interface ChatService {

    /**
     *获取好友列表
     */
    @POST("user/get_userfriends_by_userId")
    suspend fun getFriendList(): BaseResult<List<ChatUserModel>>

    /**
     * 同意或拒绝好友
     * @param isAgree 1同意 0不同意
     */
    @FormUrlEncoded
    @POST("user/agreeFriends")
    suspend fun agreeAddFriend(@Field("friendId") friendId: String, @Field("isAgree") isAgree: String): BaseResult<String>

    /**
     * 发送好友验证消息
     */
    @FormUrlEncoded
    @POST("user/sendFriendsMessage")
    suspend fun sendAddFriends(@Field("friendId") friendId: String, @Field("message") message: String): BaseResult<String>


    /**
     * 移除好友
     * /user/remove_friends
     */
    @FormUrlEncoded
    @POST("user/remove_friends")
    suspend fun removeFriend(@Field("friendId") friendId: String): BaseResult<String>


    /**
     * 发送文本消息
     */
    @FormUrlEncoded
    @POST("user/send_msg")
    suspend fun sendTextMsg(@Field("friendId") friendId: String, @Field("context") context: String): BaseResult<String>

    /**
     * 发送语音图片消息
     * friendId 好友ID
     * fileType 文件类型
     * file 文件
     */
    @POST("user/sendPicAndAudioFileMsg")
    suspend fun sendPicOrVoiceMsg(@Body body: RequestBody): BaseResult<String>

    /**
     * 根据昵称和手机号获取用户信息
     * @param nickName 用户昵称
     * @param phone
     */
    @FormUrlEncoded
    @POST("user/get_user_by_nickname")
    suspend fun getUserInfoByNickNameOrPhone(@Field("nickName") nickName: String = "", @Field("mobilePhone") phone: String = ""): BaseResult<List<ChatUserModel>>


}