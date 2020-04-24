package com.guangzhida.xiaomai

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.multidex.MultiDexApplication
import com.google.gson.Gson
import com.guangzhida.xiaomai.ext.KtxLifeCycleCallBack
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.*
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMOptions


class BaseApplication : MultiDexApplication() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    private var mServiceGson by Preference(Preference.SERVICE_GSON, "") //客服对象
    private val mGson = Gson()
    var mUserModel: UserModel.Data? = null
    var mServiceModel: ChatUserModel? = null
    override fun onCreate() {
        super.onCreate()
        instance = this
        mUserModel = mGson.fromJson<UserModel.Data>(mUserGson, UserModel.Data::class.java)
        mServiceModel = mGson.fromJson<ChatUserModel>(mServiceGson, ChatUserModel::class.java)
        Utils_CrashHandler.getInstance().init(instance)
        LogUtils.init()
        ToastUtils.init(this)
        EaseUiHelper.init(this.applicationContext, packageName)
        registerActivityLifecycleCallbacks(KtxLifeCycleCallBack())
    }

    /**
     * 获取MyApplication得单例
     */
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: BaseApplication? = null

        fun instance() = instance!!
    }
}