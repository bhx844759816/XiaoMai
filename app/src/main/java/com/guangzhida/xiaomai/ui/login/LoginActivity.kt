package com.guangzhida.xiaomai.ui.login

import android.os.Bundle
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.*
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.rsAEncode
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
        registerLiveDataObserver()
    }

    /**
     * 注册LiveData观察者
     */
    private fun registerLiveDataObserver() {
        mViewModel.mUserModelData.observe(this, Observer {
            //登录成功
            BaseApplication.instance().userModel = it
            BaseApplication.instance().mToken = it.token
            SPUtils.put(this, USER_TOKEN_KEY, it.token)
            //将手机号 密码保存到本地MD5加密
            if (!phone.isNullOrEmpty() && !password.isNullOrEmpty()) {
                SPUtils.put(this, USER_ACCOUNT_KEY, phone)
                SPUtils.put(this, USER_PASSWORD_KEY, Base64Util.encodeWord(password))
            }
        })
        mViewModel.mLoginResult.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }


}