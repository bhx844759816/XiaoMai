package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModelWrap
import retrofit2.Call
import java.util.concurrent.Callable

class HomeRepository(netWork: HomeNetWork) {
    private val mNetWork = netWork


    suspend fun getAccountInfo(phone: String): AccountModel {
        return mNetWork.getAccountInfo(phone)
    }

    suspend fun getSchoolInfo(): SchoolModelWrap {
        return mNetWork.getSchoolModelInfo()
    }

    companion object {

        @Volatile
        private var INSTANCE: HomeRepository? = null

        fun getInstance(netWork: HomeNetWork) =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: HomeRepository(netWork).also { INSTANCE = it }
            }
    }
}