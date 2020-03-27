package com.guangzhida.xiaomai.http.interceptor

import com.guangzhida.xiaomai.BaseApplication
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 添加通用请求头
 */
class BaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().newBuilder().run {
            if (!BaseApplication.instance().mToken.isNullOrEmpty()) {
                addHeader("Authorization",BaseApplication.instance().mToken!!).build()
            }
            build()
        })
    }
}