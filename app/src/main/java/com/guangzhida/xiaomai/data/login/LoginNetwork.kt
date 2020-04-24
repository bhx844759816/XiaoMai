package com.guangzhida.xiaomai.data.login

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.UserModel

class LoginNetwork {

    private val mService by lazy { RetrofitManager.getInstance().create(LoginService::class.java) }

    /**
     * 发送验证码
     */
    suspend fun sendSmsCode(phone: String, type: String): BaseResult<String> {
        return mService.sendSmsCode(phone, type)
    }

    /**
     * 注册
     */
    suspend fun register(phone: String, smsCode: String, password: String): BaseResult<String> {
        return mService.register(phone, smsCode, password)
    }

    /**
     *登录
     */
    suspend fun login(phone: String, password: String): UserModel {
        return mService.loginForToken(phone, password)
    }

    suspend fun getUseInfoByUserName(userName: String): BaseResult<List<ChatUserModel>> {
        return mService.getUserInfoByNickNameOrPhone(userName)
    }

    /**
     * 刷新Token
     */
    suspend fun refreshToken(): BaseResult<String> {
        return mService.refreshToken()
    }

    /**
     * 验证token
     */
    suspend fun verifyToken(token: String): BaseResult<String> {
        return mService.verifyToken(token)
    }

    /**
     * 修改密码
     */
    suspend fun modifyPassword(
        phone: String,
        smsCode: String,
        password: String,
        ensurePwd: String
    ): BaseResult<String> {
        return mService.modifyPassword(phone, smsCode, password, ensurePwd)
    }

    companion object {
        @Volatile
        private var netWork: LoginNetwork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: LoginNetwork().also { netWork = it }
            }
    }
}