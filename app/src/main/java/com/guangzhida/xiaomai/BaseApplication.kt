package com.guangzhida.xiaomai

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.multidex.MultiDexApplication
import com.fengchen.uistatus.UiStatusManager
import com.fengchen.uistatus.annotation.UiStatus
import com.fengchen.uistatus.controller.IUiStatusController
import com.fengchen.uistatus.listener.OnRetryListener
import com.google.gson.Gson
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.ext.KtxLifeCycleCallBack
import com.guangzhida.xiaomai.ext.KtxManager
import com.guangzhida.xiaomai.ext.jumpLoginByState
import com.guangzhida.xiaomai.ktxlibrary.ext.dp2px
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.utils.Utils_CrashHandler
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator
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


        mAccountModel =
            mGson.fromJson<AccountModel>(mSchoolAccountInfoGson, AccountModel::class.java)
        registerActivityLifecycleCallbacks(KtxLifeCycleCallBack())
        Utils_CrashHandler.getInstance().init(instance)
        LogUtils.init()
        ToastUtils.init(this)
        ChatHelper.init(this.applicationContext, packageName)
        initSmartRefresh()
        initUiStatus()
        LogUtils.i("app mUserModel=$mUserModel")
    }

    private fun initSmartRefresh() {
        SmartRefreshLayout.setDefaultRefreshInitializer { _, layout ->
            //全局设置（优先级最低）
            layout.setEnableAutoLoadMore(true)
            layout.setEnableOverScrollDrag(false)
            layout.setEnableOverScrollBounce(true)
            layout.setEnableLoadMoreWhenContentNotFull(false)
            layout.setEnableScrollContentWhenRefreshed(true)
            layout.setEnableFooterFollowWhenNoMoreData(true) //显示底部布局
            layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white)
        }
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            MaterialHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            layout.setDisableContentWhenLoading(false)
            val footer = ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate)
            footer
        }
    }

    private fun initUiStatus() {
        UiStatusManager.getInstance()
            .addUiStatusConfig(UiStatus.LOADING, R.layout.view_state_loading_layout)
            .addUiStatusConfig(UiStatus.EMPTY, R.layout.view_state_empty_layout)
            .addUiStatusConfig(
                UiStatus.NOT_FOUND, R.layout.view_state_not_login_layout, R.id.tvLogin
            ) { p0, p1, p2 ->
                KtxManager.currentActivity?.jumpLoginByState()
            }
            .addUiStatusConfig(
                UiStatus.NETWORK_ERROR,
                R.layout.view_state_error_layout,
                R.id.tvRetry,
                null
            )

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