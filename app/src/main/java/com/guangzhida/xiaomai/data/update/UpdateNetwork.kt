package com.guangzhida.xiaomai.data.update

import com.guangzhida.xiaomai.http.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class UpdateNetwork {
    private val mService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(10,TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(UpdateService::class.java)
    }

    suspend fun asyncGet(url: String, map: Map<String, String>): ResponseBody {
        return mService.asyncGet(url, map)
    }

    suspend fun asyncPost(url: String, map: MutableMap<String, RequestBody>): ResponseBody {
        return mService.asyncPost(url, map)
    }

    suspend fun download(url: String): ResponseBody {
        return mService.download(url)
    }

    companion object {
        @Volatile
        private var netWork: UpdateNetwork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: UpdateNetwork().also { netWork = it }
            }
    }
}