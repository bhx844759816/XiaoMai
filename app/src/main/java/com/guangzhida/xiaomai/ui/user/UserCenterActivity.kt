package com.guangzhida.xiaomai.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.login.ForgetPasswordActivity
import com.guangzhida.xiaomai.ui.user.viewmodel.UserCenterModel
import kotlinx.android.synthetic.main.activity_user_center_layout.*

/**
 * 个人中心
 */
class UserCenterActivity : BaseActivity<UserCenterModel>() {
    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        rlUserMessage.setOnClickListener {
            startKtxActivity<UserMessageActivity>()
        }
        rlModifyPassword.setOnClickListener {
            startKtxActivity<ForgetPasswordActivity>()
        }
    }

    override fun layoutId(): Int =R.layout.activity_user_center_layout

}