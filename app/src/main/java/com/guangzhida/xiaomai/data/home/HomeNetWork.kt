package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolInfoModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.model.SchoolModelWrap
import com.guangzhida.xiaomai.utils.LogUtils
import retrofit2.Call
import java.util.concurrent.Callable

class HomeNetWork {

    private val mService by lazy { RetrofitManager.getInstance().create(HomeService::class.java) }

    suspend fun getAccountInfo(phone: String): AccountModel {
        return mService.getAccountInfo(phone)
    }

    suspend fun getSchoolModelInfo(): SchoolModelWrap {
        return mService.getSchoolInfo()
    }

    /**
     * 获取学校信息通过学校地址
     */
    suspend fun getSchoolInfoByName(schoolName: String): SchoolInfoModel {
        return mService.getSchoolInfoByName(schoolName)
    }

    /**
     * 一键认证
     */

    suspend fun doAccountVerify(url: String, params: Map<String, String?>): String {
        LogUtils.i("一键认证:${params.toString()}")
        return mService.doAccountVerify(url,params)
    }

    /**
     * 退出认证
     */
    suspend fun exitAccountVerify(url: String, params: Map<String, String?>): String {
        return mService.exitAccountVerify(url, params)
    }

    companion object {
        @Volatile
        private var netWork: HomeNetWork? = null

        fun getInstance() = netWork
            ?: synchronized(this) {
                netWork
                    ?: HomeNetWork().also { netWork = it }
            }
    }


}