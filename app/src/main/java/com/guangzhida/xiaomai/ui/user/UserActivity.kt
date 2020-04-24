package com.guangzhida.xiaomai.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.BindAccountDialog
import com.guangzhida.xiaomai.dialog.SelectSchoolDialog
import com.guangzhida.xiaomai.event.schoolModelChangeLiveData
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.ui.home.HomeFragment
import com.guangzhida.xiaomai.ui.home.viewmodel.HomeViewModel
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.ui.user.viewmodel.UserViewModel
import com.guangzhida.xiaomai.utils.NetworkUtils
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_user_layout.*

/**
 * 个人中心界面
 */
class UserActivity : BaseActivity<UserViewModel>() {
    //存储本地绑定的账号信息
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")
    private val mAccountModel by lazy {
        Gson().fromJson<AccountModel>(mSchoolAccountInfoGson, AccountModel::class.java)
    }

    override fun layoutId(): Int = R.layout.activity_user_layout

    override fun initView(savedInstanceState: Bundle?) {
        StatusBarUtil.setTranslucentForImageView(this, 0, toolBar)
        initUserEntity()
        tvSchoolName.text = mAccountModel?.name ?: "未绑定"
        tvSchoolAccount.text =
            if (mAccountModel?.user.isNullOrEmpty()) "未绑定" else mAccountModel?.user
    }

    override fun initListener() {
        //关闭页面
        rlFinish.setOnClickListener { finish() }
        //个人中心
        rlUserCenter.setOnClickListener {
            if (BaseApplication.instance().mUserModel == null) {
                ToastUtils.toastShort("请先登录")
                startKtxActivity<LoginActivity>()
            } else {
                startKtxActivity<UserCenterActivity>()
            }
        }
        //点击名称
        tvUserName.setOnClickListener {
            if (BaseApplication.instance().mUserModel == null) {
                startKtxActivity<LoginActivity>()
            }
        }
        //点击头像
        ivHeaderView.setOnClickListener {
            if (BaseApplication.instance().mUserModel == null) {
                startKtxActivity<LoginActivity>()
            }
        }
        //系统设置
        rlSystemSetting.setOnClickListener {
            startKtxActivity<SettingActivity>()
        }
        userModelChangeLiveData.observe(this, Observer {
            initUserEntity()
        })
    }

    private fun initUserEntity() {
        if (BaseApplication.instance().mUserModel == null) {
            tvUserName.text = "点击登录/注册"
            ivHeaderView.setBackgroundResource(R.mipmap.icon_user_center_header)
        } else {
            ivHeaderView.loadCircleImage(
                BASE_URL.substring(
                    0,
                    BASE_URL.length - 1
                ) + BaseApplication.instance().mUserModel!!.headUrl,
                holder = R.mipmap.icon_user_center_header
            )
            tvUserName.text = BaseApplication.instance().mUserModel!!.name
        }

    }
}