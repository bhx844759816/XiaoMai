package com.guangzhida.xiaomai.data.login

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.UserModel
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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

}