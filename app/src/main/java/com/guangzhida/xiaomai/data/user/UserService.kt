package com.guangzhida.xiaomai.data.user

import com.guangzhida.xiaomai.base.BaseResult
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
    suspend fun updateUserInfo(@FieldMap params: Map<String, String>):ModifyUserModel

    /**
     * 用户登录获取token
     */
    @FormUrlEncoded
    @POST("jwt/token")
    suspend fun loginForToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): UserModel



}