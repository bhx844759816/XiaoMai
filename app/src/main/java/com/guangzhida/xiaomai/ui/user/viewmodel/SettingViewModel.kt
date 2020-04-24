package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.AppCacheUtils
import com.guangzhida.xiaomai.utils.Preference
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class SettingViewModel : BaseViewModel() {
    val doLogoutResultLiveDta = MutableLiveData<Boolean>()
    val caseSizeResultLiveDta = MutableLiveData<String>()
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    private var mServiceGson by Preference(Preference.SERVICE_GSON, "") //客服对象

    /**
     * 退出登录
     */
    fun doLogout() {
        launchUI {
            defUI.showDialog.call()
            try {
                withContext(Dispatchers.IO) {
                    BaseApplication.instance().mUserModel = null
                    BaseApplication.instance().mServiceModel = null
                    mUserGson = ""
                    mServiceGson = ""
                    EMClient.getInstance().logout(true)
                }
                defUI.toastEvent.postValue("退出登录成功")
                defUI.dismissDialog.call()
                doLogoutResultLiveDta.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                doLogoutResultLiveDta.postValue(false)
                defUI.dismissDialog.call()
            }

        }
    }

    /**
     * 获取缓存大小
     */
    fun getCacheSize(){
        launchUI {
            val size = withContext(Dispatchers.IO){
                AppCacheUtils.getTotalCacheSize(BaseApplication.instance().applicationContext)
            }
            caseSizeResultLiveDta.postValue(size)
        }
    }

    /**
     * 清理缓存
     */
    fun clearCache(){
        launchUI {
            val size = withContext(Dispatchers.IO){
                AppCacheUtils.clearAllCache(BaseApplication.instance().applicationContext)
                AppCacheUtils.getTotalCacheSize(BaseApplication.instance().applicationContext)
            }
            caseSizeResultLiveDta.postValue(size)
        }
    }
}