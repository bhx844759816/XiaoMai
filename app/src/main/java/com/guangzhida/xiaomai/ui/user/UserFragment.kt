package com.guangzhida.xiaomai.ui.user

import android.os.Bundle
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.ext.loadImage
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.chat.ServiceActivity
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.ui.user.viewmodel.UserViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_user_layout.*

/**
 * 我的界面
 */
class UserFragment : BaseFragment<UserViewModel>() {
    override fun layoutId(): Int = R.layout.fragment_user_layout

    override fun initView(savedInstanceState: Bundle?) {
        initObserver()
        showUserInfo()
    }

    override fun initListener() {
        llMyPublish.clickN {
            if (BaseApplication.instance().mUserModel != null) {
                startKtxActivity<MyPublishActivity>()
            } else {
                ToastUtils.toastShort("登录后才能查看")
            }
        }
        llMySignUp.clickN {
            if (BaseApplication.instance().mUserModel != null) {
                startKtxActivity<MySignUpActivity>()
            } else {
                ToastUtils.toastShort("登录后才能查看")
            }
        }
        llUserCenter.clickN {
            if (BaseApplication.instance().mUserModel != null) {
                startKtxActivity<UserCenterActivity>()
            } else {
                ToastUtils.toastShort("登录后才能查看")
            }
        }

        rlUserFeedBack.clickN {
            startKtxActivity<UserFeedBackActivity>()
        }

        rlUserSetting.clickN {
            startKtxActivity<SettingActivity>()
        }

        llContactServer.clickN {
            startKtxActivity<ServiceActivity>()
        }
        ivUserAvatar.clickN {
            if (BaseApplication.instance().mUserModel == null) {
                startKtxActivity<LoginActivity>()
            } else {
                startKtxActivity<UserMessageActivity>()
            }
        }
        tvUserName.clickN {
            if (BaseApplication.instance().mUserModel == null) {
                startKtxActivity<LoginActivity>()
            } else {
                startKtxActivity<UserMessageActivity>()
            }
        }
    }


    private fun initObserver() {
        //用户信息改变
        userModelChangeLiveData.observe(this, Observer {
            showUserInfo()
        })
    }

    private fun showUserInfo() {
        if (BaseApplication.instance().mUserModel != null) {
            //加载头像
            ivUserAvatar.loadCircleImage(BASE_URL_IMG + BaseApplication.instance().mUserModel!!.headUrl)
            tvUserName.text = BaseApplication.instance().mUserModel!!.name
        } else {
            ivUserAvatar.loadImage(R.mipmap.icon_user_center_header)
            tvUserName.text = "未登录"
        }
    }
}