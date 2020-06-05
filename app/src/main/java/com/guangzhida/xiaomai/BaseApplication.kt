package com.guangzhida.xiaomai

import android.annotation.SuppressLint
import androidx.multidex.MultiDexApplication
import com.google.gson.Gson
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.ext.KtxLifeCycleCallBack
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.utils.*
import com.tencent.bugly.crashreport.CrashReport


class BaseApplication : MultiDexApplication() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    private var mSchoolAccountInfoGson by Preference(
        Preference.SCHOOL_NET_ACCOUNT_GSON,
        ""
    ) //绑定的账号信息
    private val mGson = Gson()
    var mUserModel: UserModel.Data? = null
    var mAccountModel: AccountModel? = null
    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashReport.initCrashReport(instance, "d658129c05", false);
        mUserModel = mGson.fromJson<UserModel.Data>(mUserGson, UserModel.Data::class.java)
        mAccountModel = mGson.fromJson<AccountModel>(mSchoolAccountInfoGson, AccountModel::class.java)
        Utils_CrashHandler.getInstance().init(instance)
        LogUtils.init()
        ToastUtils.init(this)
        ChatHelper.init(this.applicationContext, packageName)
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