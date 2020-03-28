package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolInfoModel
import com.guangzhida.xiaomai.model.SchoolModel
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

    /**
     * 认证
     */
    suspend fun doAccountVerify(url: String, params: Map<String, String?>): String {
        return mNetWork.doAccountVerify(url, params)
    }

    /**
     * 退出认证
     */
    suspend fun exitAccountVerify(url: String, params: Map<String, String?>): String {
        return mNetWork.exitAccountVerify(url, params)
    }

    /**
     * 获取学校信息通过学校名称
     */
    suspend fun getSchoolInfoByName(schoolName: String): SchoolInfoModel {
        return mNetWork.getSchoolInfoByName(schoolName)
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