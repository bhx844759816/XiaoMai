package com.guangzhida.xiaomai.data.chat

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.base.PageResult
import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.FieldMap
import java.io.File

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
    suspend fun sendPicOrVoiceMsg(
        friendId: String,
        fileType: String,
        file: File
    ): BaseResult<String> {
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        val friendBody = RequestBody.create(MediaType.parse("text/plain"), friendId)
        val fileTypeBody = RequestBody.create(MediaType.parse("text/plain"), fileType)
        return mService.sendPicOrVoiceMsg(part, friendBody, fileTypeBody)
    }

    /**
     * 根据昵称和手机号获取用户信息
     */
    suspend fun getUserInfoByNickNameOrPhone(
        nickName: String = "",
        phone: String = ""
    ): BaseResult<List<ChatUserModel>> {
        return mService.getUserInfoByNickNameOrPhone(nickName, phone)
    }

    /**
     * 根据昵称和手机号获取用户信息
     */
    suspend fun getUserInfoByNickNameOrPhone(
        nickNameOrPhone: String
    ): BaseResult<List<ChatUserModel>> {
        return mService.getUserInfoByNickNameOrPhone(nickNameOrPhone)
    }

    fun getUserInfoSync(nickNameOrPhone: String): Call<BaseResult<List<ChatUserModel>>> {
        return mService.getUserInfoSync(nickNameOrPhone)
    }

    suspend fun getServiceProblemList(): BaseResult<List<ServiceProblemModel>> {

        return mService.getServiceProblemList()
    }

    /**
     * 通过学校ID获取在线客服
     */
    suspend fun getOnlineServer(schoolId: String): BaseResult<ServiceModel> {
        return mService.getOnlineServer(schoolId)
    }

    /**
     * 上传约吗发布的活动
     */
    suspend fun submitAppointmentData(params: MutableMap<String, Any>): BaseResult<String> {
        return mService.submitAppointmentData(params)
    }

    /**
     * 获取发布的约吗
     */
    suspend fun getAppointmentData(
        schoolId: String,
        userId: String,
        type: String,
        limit: String, page: String
    ): PageResult<AppointmentModel> {
        return mService.getAppointmentData(schoolId, userId,type, limit, page)
    }

    suspend fun signUpActivity(
        schoolId: String,
        userId: String,
        aboutId: String,
        isCancel: String
    ): BaseResult<String> {
        return mService.signUpActivity(schoolId, userId, aboutId, isCancel)
    }

    suspend fun getSignUpUserByActivityId(activityId: String): BaseResult<List<ChatUserModel>>{
        return mService.getSignUpUserByActivityId(activityId)
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