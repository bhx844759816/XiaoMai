package com.guangzhida.xiaomai.data.home

import com.guangzhida.xiaomai.http.RetrofitManager
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModelWrap
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