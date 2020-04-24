package com.guangzhida.xiaomai.data.user

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.data.login.LoginService
import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.ModifyUserModel
import com.guangzhida.xiaomai.model.UploadMessageModel
import com.guangzhida.xiaomai.model.UserModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 用户
 */
class UserNetWork {

    private val mService by lazy { RetrofitManager.getInstance().create(UserService::class.java) }

    /**
     * 上传图片
     */
    suspend fun uploadImg(file: File): UploadMessageModel {
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        return mService.uploadImg(part)
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(params: Map<String, String>): ModifyUserModel {
        return mService.updateUserInfo(params)
    }


    companion object {
        @Volatile
        private var netWork: UserNetWork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: UserNetWork().also { netWork = it }
            }
    }
}