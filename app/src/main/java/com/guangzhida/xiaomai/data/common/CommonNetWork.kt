package com.guangzhida.xiaomai.data.common

import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.UploadMessageModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import java.io.File

class CommonNetWork {
    private val mService by lazy { RetrofitManager.getInstance().create(CommonService::class.java) }

    suspend fun uploadImg(file: File): UploadMessageModel {
        val body = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        return mService.uploadImg(part)
    }

    companion object {
        @Volatile
        private var netWork: CommonNetWork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: CommonNetWork().also { netWork = it }
            }
    }
}