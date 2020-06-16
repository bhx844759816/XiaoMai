package com.guangzhida.xiaomai.utils

import okhttp3.*
import java.io.IOException


object HttpUtils {

    private val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
    private val client = OkHttpClient()

    @Throws(IOException::class)
    fun post(url: String, json: String?): Response? {
        val body = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return client.newCall(request).execute()
    }
    @Throws(IOException::class)
    fun post(url: String, form: Map<String,String>): Response? {
       val formBodyBuild =  FormBody.Builder()
        form.forEach {
            formBodyBuild.add(it.key,it.value)
        }
        val request: Request = Request.Builder()
            .url(url)
            .post(formBodyBuild.build())
            .build()
        return client.newCall(request).execute()
    }

}