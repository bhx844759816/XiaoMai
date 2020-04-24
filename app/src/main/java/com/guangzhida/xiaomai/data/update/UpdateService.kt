package com.guangzhida.xiaomai.data.update

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * 下载更新的Service
 */
interface UpdateService {

    /**
     * 异步GET请求
     */
    @GET
    suspend fun asyncGet(@Url url: String, @QueryMap map: Map<String, String>): ResponseBody

    /**
     * 异步post请求
     */
    @Multipart
    @POST
    suspend fun asyncPost(@Url url: String, @PartMap map: MutableMap<String, RequestBody>): ResponseBody

    /**
     * 下载文件
     */
    @GET
    @Streaming
    suspend fun download(@Url url: String): ResponseBody
}