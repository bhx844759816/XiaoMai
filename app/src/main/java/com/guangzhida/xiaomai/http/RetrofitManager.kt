package com.guangzhida.xiaomai.http

import com.guangzhida.xiaomai.http.interceptor.BaseInterceptor
import com.guangzhida.xiaomai.http.interceptor.LoggingInterceptor
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "http://www.app.guangzhida.cn:8762/api/admin/"

class RetrofitManager {
    private var retrofit: Retrofit? = null

    //    companion object {
//        fun getInstance() = SingletonHolder.INSTANCE
//        private lateinit var retrofit: Retrofit
//    }
//    private object SingletonHolder {
//        val INSTANCE = RetrofitManager()
//    }
    companion object {
        @Volatile
        private var retrofitManager: RetrofitManager? = null

        fun getInstance() = retrofitManager
            ?: synchronized(this) {
                retrofitManager
                    ?: RetrofitManager().also { retrofitManager = it }
            }
    }


    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .addInterceptor(LoggingInterceptor())
            .addInterceptor(BaseInterceptor())
            .writeTimeout(10L, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 15, TimeUnit.SECONDS))
            .build()
    }

    fun <T> create(service: Class<T>?): T =
        retrofit?.create(service!!) ?: throw RuntimeException("Api service is null!")
}