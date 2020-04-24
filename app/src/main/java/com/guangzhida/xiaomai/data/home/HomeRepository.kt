package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import java.util.concurrent.Callable

class HomeRepository(netWork: HomeNetWork) {
    private val mNetWork = netWork


    suspend fun getAccountInfo(phone: String): AccountModel {
        return mNetWork.getAccountInfo(phone)
    }

    suspend fun getPackageInfo(phone: String): ResponseBody {
        return mNetWork.getPackageInfo(phone)
    }

    suspend fun getSchoolInfo(): SchoolModelWrap {
        return mNetWork.getSchoolModelInfo()
    }

    /**
     * 认证
     */
    suspend fun doAccountVerify(url: String, params: Map<String, String?>): VerifyModel {
        return mNetWork.doAccountVerify(url, params)
    }

    /**
     * 退出认证
     */
    suspend fun exitAccountVerify(url: String, params: Map<String, String?>): VerifyModel {
        return mNetWork.exitAccountVerify(url, params)
    }

    /**
     *
     */
    suspend fun modifyAccountPassword(url: String, params: Map<String, String?>): ResponseBody {
        return mNetWork.modifyAccountPassword(url, params)
    }

    /**
     * 获取学校信息通过学校名称
     */
    suspend fun getSchoolInfoByName(schoolName: String): SchoolInfoModel {
        return mNetWork.getSchoolInfoByName(schoolName)
    }

    /**
     * 清空套餐信息
     */
    suspend fun clearAccountPackage(url: String, params: Map<String, String?>): ResponseBody {
        return mNetWork.clearAccountPackage(url, params)
    }

    /**
     * 绑定校园卡套餐
     */
    suspend fun bindSchoolAccount(url: String, params: Map<String, String?>): ResponseBody {
        return mNetWork.bindSchoolAccount(url, params)
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