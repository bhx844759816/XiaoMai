package com.guangzhida.xiaomai.data.common

import com.guangzhida.xiaomai.model.UploadMessageModel
import java.io.File

class CommonRepository(val netWork: CommonNetWork) {
    private val mNetWork = netWork

    suspend fun uploadImg(file: File): UploadMessageModel {
        return mNetWork.uploadImg(file)
    }


    companion object {
        @Volatile
        private var INSTANCE: CommonRepository? = null

        fun getInstance(netWork: CommonNetWork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: CommonRepository(netWork).also { INSTANCE = it }
            }
    }
}