package com.guangzhida.xiaomai.data.login

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.UserModel

class LoginRepository(netWork: LoginNetwork) {
    private val mNetwork = netWork

    /**
     * 发送验证码
     */
    suspend fun sendSmsCode(phone: String, type: String): BaseResult<String> {
        return mNetwork.sendSmsCode(phone, type)
    }

    /**
     * 注册
     */
    suspend fun register(phone: String, smsCode: String, password: String): BaseResult<String> {
        return mNetwork.register(phone, smsCode, password)
    }

    /**
     *登录
     */
    suspend fun login(phone: String, password: String): UserModel {
        return mNetwork.login(phone, password)
    }

    companion object {

        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(netWork: LoginNetwork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: LoginRepository(netWork).also { INSTANCE = it }
            }
    }
}