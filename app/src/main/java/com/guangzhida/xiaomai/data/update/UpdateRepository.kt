package com.guangzhida.xiaomai.data.update

import okhttp3.RequestBody
import okhttp3.ResponseBody

class UpdateRepository(netWork: UpdateNetwork) {
    private val mNetwork = netWork
    suspend fun asyncGet(url: String, map: Map<String, String>): ResponseBody {
        return mNetwork.asyncGet(url, map)
    }

    suspend fun asyncPost(url: String, map: MutableMap<String, RequestBody>): ResponseBody {
        return mNetwork.asyncPost(url, map)
    }

    suspend fun download(url: String): ResponseBody {
        return mNetwork.download(url)
    }

    companion object {
        @Volatile
        private var INSTANCE: UpdateRepository? = null

        fun getInstance(netWork: UpdateNetwork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: UpdateRepository(netWork).also { INSTANCE = it }
            }
    }
}