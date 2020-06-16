package com.guangzhida.xiaomai.data.user

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.data.chat.ChatNetWork
import com.guangzhida.xiaomai.data.chat.ChatRepository
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.ModifyUserModel
import com.guangzhida.xiaomai.model.UploadMessageModel
import com.guangzhida.xiaomai.model.UserModel
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

    suspend fun getUserInfoByUserId(userId: String): UserModel {
        return mNetWork.getUserInfoByUserId(userId)
    }

    /**
     * 获取我发布的约吗
     */
    suspend fun getMyPublishAppointmentList(
        userId: String,
        schoolId: String
    ): BaseResult<List<AppointmentModel>> {
        return mNetWork.getMyPublishAppointmentList(userId, schoolId)
    }

    /**
     * 获取我报名的约吗
     */
    suspend fun getMySignUpAppointmentList(
        userId: String,
        schoolId: String
    ): BaseResult<List<AppointmentModel>> {
        return mNetWork.getMySignUpAppointmentList(userId, schoolId)
    }

    /**
     * 删除我的发布
     */
    suspend fun deleteMyPublishAppointment(aboutId: String): BaseResult<String> {
        return mNetWork.deleteMyPublishAppointment(aboutId)
    }

    /**
     * 删除我的报名
     */
    suspend fun deleteMySignUpAppointment(aboutId: String): BaseResult<String> {
        return mNetWork.deleteMySignUpAppointment(aboutId)
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