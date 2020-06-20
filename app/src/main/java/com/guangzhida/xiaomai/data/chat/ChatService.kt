package com.guangzhida.xiaomai.data.chat

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.base.PageResult
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

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

//    /**
//     * 发送语音图片消息
//     * friendId 好友ID
//     * fileType 文件类型
//     * file 文件
//     */
//    @Headers("Content-type:application/json")
//    @POST("user/sendPicAndAudioFileMsg")
//    suspend fun sendPicOrVoiceMsg(@Body body: RequestBody): BaseResult<String>
    /**
     * 发送语音图片消息
     * friendId 好友ID
     * fileType 文件类型
     * file 文件
     */
    @Multipart
    @POST("user/sendPicAndAudioFileMsg")
    suspend fun sendPicOrVoiceMsg(
        @Part photo: MultipartBody.Part,
        @Part("friendId") friendId: RequestBody, @Part("fileType") fileType: RequestBody
    ): BaseResult<String>

    /**
     * 根据昵称和手机号获取用户信息
     * @param nickName 用户昵称
     * @param phone
     */
    @FormUrlEncoded
    @POST("user/get_user_by_nickname")
    suspend fun getUserInfoByNickNameOrPhone(@Field("nickName") nickName: String = "", @Field("mobilePhone") phone: String = ""): BaseResult<List<ChatUserModel>>


    @FormUrlEncoded
    @POST("user/get_user_by_nickorphone")
    suspend fun getUserInfoByNickNameOrPhone(@Field("nickOrPhone") nickOrPhone: String): BaseResult<List<ChatUserModel>>

    /**
     * 同步获取
     */
    @FormUrlEncoded
    @POST("user/get_user_by_nickorphone")
    fun getUserInfoSync(@Field("nickOrPhone") nickOrPhone: String): Call<BaseResult<List<ChatUserModel>>>

    /**
     * 获取在线客服的问题列表
     */
    @GET("problem/get_problem")
    suspend fun getServiceProblemList(): BaseResult<List<ServiceProblemModel>>

    /**
     * 通过学校ID获取在线客服对象
     */
    @FormUrlEncoded
    @POST("network/customer_service/getCustomerServiceByOnline")
    suspend fun getOnlineServer(@Field("schoolId") schoolId: String): BaseResult<ServiceModel>

    /**
     * 上传约吗数据
     */
    @FormUrlEncoded
    @POST("network/activity_about/addActivityAbout")
    suspend fun submitAppointmentData(@FieldMap params: MutableMap<String, Any>): BaseResult<String>

    /**
     * 获取当前学校发布的所有约吗数据
     */
    @FormUrlEncoded
    @POST("network/activity_about/getAllActivityAbout")
    suspend fun getAppointmentData(
        @Field("schoolId") schoolId: String,
        @Field("userId") userId: String,
        @Field("type") type: String,
        @Field("limit") limit: String,
        @Field("page") page: String
    ): PageResult<AppointmentModel>

    /**
     * 报名活动
     * @param aboutId 活动ID
     * @param isCancel 1取消 0不取消
     */
    @FormUrlEncoded
    @POST("network/activity_about/addAboutPeople")
    suspend fun signUpActivity(
        @Field("schoolId") schoolId: String,
        @Field("userId") userId: String,
        @Field("aboutId") aboutId: String,
        @Field("isCancel") isCancel: String
    ): BaseResult<String>


    @FormUrlEncoded
    @POST("network/activity_about/getSignUserByAboutId")
    suspend fun getSignUpUserByActivityId(@Field("aboutId") activityId: String): BaseResult<List<ChatUserModel>>
}