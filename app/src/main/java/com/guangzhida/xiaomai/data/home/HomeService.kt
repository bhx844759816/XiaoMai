package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.Callable

interface HomeService {
    @FormUrlEncoded
    @POST("http/apihttp")
    suspend fun getAccountInfo(@Field("user") phone: String): AccountModel

    /**
     * 获取套餐信息
     */
    @FormUrlEncoded
    @POST("http/apipacKage")
    suspend fun getPackageInfo(@Field("user") phone: String): ResponseBody


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

    @FormUrlEncoded
    @POST("")
    suspend fun doAccountVerify2(@Url url: String, @FieldMap params: Map<String, String?>): ResponseBody

    /**
     * 退出认证
     */
    @GET("")
    suspend fun exitAccountVerify(@Url url: String, @QueryMap params: Map<String, String?>): VerifyModel

    /**
     *
     * http://yonghu.guangzhida.cn/lfradius/api.php
     */
    @FormUrlEncoded
    @POST()
    suspend fun modifyAccountPassword(@Url url: String, @FieldMap params: Map<String, String?>): ResponseBody

    /**
     *
     * 清空套餐信息
     */
    @FormUrlEncoded
    @POST("")
    suspend fun clearAccountPackage(@Url url: String, @FieldMap params: Map<String, String?>): ResponseBody

    /**
     * 绑定校园卡套餐
     */
    @FormUrlEncoded
    @POST("")
    suspend fun bindSchoolAccount(@Url url: String, @FieldMap params: Map<String, String?>): ResponseBody


    /**
     *通过校园卡账号密码登录
     */
    @FormUrlEncoded
    @POST("jwt/campus_network_login")
    suspend fun doLoginBySchoolAccount(
        @Field("campusNetworkNum") schoolAccount: String,
        @Field("campusNetworkPwd") schoolPassword: String
    ): UserModel


    /**
     * 绑定校园卡
     */
    @FormUrlEncoded
    @POST("user/campus_network_bind")
    suspend fun doBindSchoolAccount(
        @Field("campusNetworkNum") schoolAccount: String,
        @Field("campusNetworkPwd") schoolPassword: String,
        @Field("id") id: String
    ): UserModel


    /**
     * 通过学校ID获取客服对象
     */
    @FormUrlEncoded
    @POST("network/customer_service/getCustomerServiceBySchool")
    suspend fun getServiceBySchoolId(@Field("schoolId") schoolId: String): BaseResult<List<ServiceModel>>

    /**
     * 通过学校ID获取弹窗广告对象
     */
    @FormUrlEncoded
    @POST("network/pop_ads/getPopAdsBySchool")
    suspend fun getPopAdBySchoolId(@Field("schoolId") schoolId: String): BaseResult<PopAdModel>

}