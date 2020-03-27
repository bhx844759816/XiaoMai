package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModelWrap
import retrofit2.Call
import retrofit2.http.*
import java.util.concurrent.Callable

interface HomeService {
    @FormUrlEncoded
    @POST("http/apihttp")
    suspend fun getAccountInfo(@Field("user") phone: String): AccountModel


    @POST("schoolinfo/get_list")
    suspend fun getSchoolInfo(): SchoolModelWrap
}