package com.guangzhida.xiaomai.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Selection
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.*
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.LoginViewModel
import com.guangzhida.xiaomai.utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<LoginViewModel>() {
    private var phone: String? = null
    private var password: String? = null
    override fun layoutId(): Int = R.layout.activity_login

    override fun initView(savedInstanceState: Bundle?) {
        idCancel.setOnClickListener {
            finish()
        }
        //注册
        idCreateAccount.setOnClickListener {
            startKtxActivity<RegisterActivity>()
        }
        //登录
        idLoginTv.setOnClickListener {
            phone = inputPhone.text.toString().trim()
            password = inputPassword.text.toString().trim()
            if (phone.isNullOrEmpty()) {
                ToastUtils.toastShort("请输入手机号")
                return@setOnClickListener
            }
            if (password.isNullOrEmpty()) {
                ToastUtils.toastShort("请输入密码")
                return@setOnClickListener
            }
            mViewModel.doLogin(phone!!, password!!)
        }
        //忘记密码
        idForgetPasswordTv.setOnClickListener {
            startKtxActivity<ForgetPasswordActivity>()
        }
        //改变按钮的隐藏显示状态
        cbPassword.setOnCheckedChangeListener { _, _ ->
            checkPasswordShowState()
        }
        registerLiveDataObserver()
    }

    /**
     * 注册LiveData观察者
     */
    private fun registerLiveDataObserver() {
        mViewModel.mLoginResult.observe(this, Observer {
            if (it) {
                userModelChangeLiveData.postValue(true)
                finish()
            }
        })
    }

    private fun checkPasswordShowState() {
        val method = inputPassword.transformationMethod
        if (method === HideReturnsTransformationMethod.getInstance()) {
            inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        } else {
            inputPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        // 保证切换后光标位于文本末尾
        val spanText = inputPassword.text
        if (spanText != null) {
            Selection.setSelection(spanText, spanText.length)
        }
    }


}