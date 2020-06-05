package com.guangzhida.xiaomai.data.user

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.data.chat.ChatNetWork
import com.guangzhida.xiaomai.data.chat.ChatRepository
import com.guangzhida.xiaomai.model.ModifyUserModel
import com.guangzhida.xiaomai.model.UploadMessageModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UserRepository(netWork: UserNetWork) {
    private val mNetWork = netWork

    suspend fun uploadImg(file: File): UploadMessageModel {
        return mNetWork.uploadImg(file)
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(params: Map<String, String>): ModifyUserModel {
        return mNetWork.updateUserInfo(params)
    }

    /**
     * 更新用户信息
     */
    suspend fun uploadUserFeedBack(params: Map<String, String>): BaseResult<String> {
        return mNetWork.uploadUserFeedBack(params)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(netWork: UserNetWork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: UserRepository(netWork).also { INSTANCE = it }
            }
    }
}