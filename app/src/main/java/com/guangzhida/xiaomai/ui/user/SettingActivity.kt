package com.guangzhida.xiaomai.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.KtxManager
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.goToAppInfoPage
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.MainActivity
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.user.viewmodel.SettingViewModel
import com.guangzhida.xiaomai.utils.Preference
import com.hyphenate.chat.EMClient
import kotlinx.android.synthetic.main.activity_setting.*

/**
 * 系统设置界面
 */
class SettingActivity : BaseActivity<SettingViewModel>() {

    override fun layoutId(): Int = R.layout.activity_setting

    override fun initView(savedInstanceState: Bundle?) {

        toolBar.setNavigationOnClickListener {
            finish()
        }
        tvLogout.setOnClickListener {
            mViewModel.doLogout()
        }
        //清除缓存
        rlCacheParent.setOnClickListener {
            mViewModel.clearCache()
        }
        //校麦服务协议
        rlCopyright.clickN {
            startKtxActivity<WebActivity>(
                values = listOf(
                    Pair("url", "file:///android_asset/ServiceAgreement.html"),
                    Pair("type", "protocol")
                )
            )
        }
        //隐私权政策
        rlPrivacyProtocol.setOnClickListener {
            startKtxActivity<WebActivity>(
                values = listOf(
                    Pair("url", "file:///android_asset/PrivacyProtocol.html"),
                    Pair("type", "protocol")
                )
            )
        }
        rlAboutUs.setOnClickListener {
            startKtxActivity<AboutUsActivity>()
        }
        //跳转到系统设置界面
        rlNotify.setOnClickListener {
            goToAppInfoPage()
        }
        mViewModel.doLogoutResultLiveDta.observe(this, Observer {
            if (it) {
                userModelChangeLiveData.postValue(true)
                KtxManager.finishOtherActivity(MainActivity::class.java)
            }
        })
        mViewModel.caseSizeResultLiveDta.observe(this, Observer {
            tvCacheSize.text = it
        })
        mViewModel.getCacheSize()
    }
}