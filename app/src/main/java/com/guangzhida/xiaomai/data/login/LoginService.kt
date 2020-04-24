package com.guangzhida.xiaomai.data.login

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.UserModel
import retrofit2.http.*

/**
 * 登录注册的服务
 */
interface LoginService {

    /**
     * 发送手机验证码
     * @param type 1注册 2改密码
     */
    @FormUrlEncoded
    @POST("common/get_regiest_code")
    suspend fun sendSmsCode(@Field("mobilePhone") phone: String, @Field("type") type: String): BaseResult<String>

    /**
     * 用户注册
     */
    @FormUrlEncoded
    @POST("user/regiest_user")
    suspend fun register(
        @Field("mobilePhone") phone: String,
        @Field("code") smsCode: String,
        @Field("password") password: String
    ): BaseResult<String>

    /**
     * 用户登录获取token
     */
    @FormUrlEncoded
    @POST("jwt/token")
    suspend fun loginForToken(
        @Field("username") username: String,
        @Field("password") password: String
    ): UserModel

    /**
     * 刷新token
     */
    @GET("jwt/refresh")
    suspend fun refreshToken(): BaseResult<String>

    /**
     * 验证token
     */
    @GET("jwt/verify")
    suspend fun verifyToken(@Query("token") token: String): BaseResult<String>


    @FormUrlEncoded
    @POST("user/get_user_by_nickname")
    suspend fun getUserInfoByNickNameOrPhone(@Field("mobilePhone") phone: String): BaseResult<List<ChatUserModel>>

    /**
     * 修改密码
     */
    @FormUrlEncoded
    @POST("user/update_password")
    suspend fun modifyPassword(
        @Field("mobilePhone") phone: String,
        @Field("code") smsCode: String,
        @Field("password") password: String,
        @Field("ensurePwd") ensurePwd: String
    ): BaseResult<String>

}