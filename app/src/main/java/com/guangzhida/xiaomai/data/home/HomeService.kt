package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.*
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.Callable

interface HomeService {
    @FormUrlEncoded
    @POST("http/apihttp")
    suspend fun getAccountInfo(@Field("user") phone: String): AccountModel


    @POST("schoolinfo/get_list")
    suspend fun getSchoolInfo(): SchoolModelWrap

    /**
     * 获取学校信息
     */
    @FormUrlEncoded
    @POST("schoolinfo/get_by_name")
    suspend fun getSchoolInfoByName(@Field("name") phone: String): SchoolInfoModel

    /**http://192.168.1.110/portal/api/v1/login
     * http://192.168.1.110/portal/api/v1/login
     * 一键认证
     */
    @FormUrlEncoded
    @POST("")
    suspend fun doAccountVerify(@Url url: String, @FieldMap params: Map<String, String?>): VerifyModel

    /**
     * 退出认证
     */
    @GET("")
    suspend fun exitAccountVerify(@Url url: String, @QueryMap params: Map<String, String?>):VerifyModel
}