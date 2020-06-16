package com.guangzhida.xiaomai.data.user

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.ModifyUserModel
import com.guangzhida.xiaomai.model.UploadMessageModel
import com.guangzhida.xiaomai.model.UserModel
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserService {
    /**
     * 上传图片
     */
    @Multipart
    @POST("common/upload_pic")
    suspend fun uploadImg(@Part photo: MultipartBody.Part): UploadMessageModel

    /**
     * 更新用户信息
     */
    @FormUrlEncoded
    @POST("user/update_user_info")
    suspend fun updateUserInfo(@FieldMap params: Map<String, String>): ModifyUserModel

    @FormUrlEncoded
    @POST("user/user_feedback/addUserFeedback")
    suspend fun uploadUserFeedBack(@FieldMap params: Map<String, String>): BaseResult<String>

    /**
     * 根据用户ID获取用户信息
     */
    @FormUrlEncoded
    @POST("user/getUserInfoByid")
    suspend fun getUserInfoByUserId(@Field("userId") userId: String): UserModel


    /**
     * 获取我发布的约吗
     */
    @FormUrlEncoded
    @POST("network/activity_about/getActivityAbouts")
    suspend fun getMyPublishAppointmentList(
        @Field("userId") userId: String,
        @Field("schoolId") schoolId: String
    ): BaseResult<List<AppointmentModel>>

    /**
     * 删除我的发布
     */
    @FormUrlEncoded
    @POST("network/activity_about/deleteAbout")
    suspend fun deleteMyPublishAppointment(@Field("aboutId") aboutId: String): BaseResult<String>

    /**
     * 删除我的报名
     */
    @FormUrlEncoded
    @POST("network/activity_about/cancelSign")
    suspend fun deleteMySignUpAppointment(@Field("signId") aboutId: String): BaseResult<String>

    /**
     * 获取我报名的
     */
    @FormUrlEncoded
    @POST("network/activity_about/getSignActivityAbouts")
    suspend fun getMySignUpAppointmentList(
        @Field("userId") userId: String,
        @Field("schoolId") schoolId: String
    ): BaseResult<List<AppointmentModel>>
}