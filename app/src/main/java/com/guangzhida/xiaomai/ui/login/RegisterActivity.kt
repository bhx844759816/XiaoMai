package com.guangzhida.xiaomai.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.RegisterViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_register_layout.*

/**
 * 注册界面
 */
class RegisterActivity : BaseActivity<RegisterViewModel>() {
    override fun layoutId(): Int = R.layout.activity_register_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initListener() {
        //发送验证码
        tvSendSmsCode.setOnClickListener {
            val phone = inputPhone.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.toastShort("请输入手机号")
                return@setOnClickListener
            }
            mViewModel.sendSmsCode(phone)
        }
        //点击注册
        tvRegister.setOnClickListener {
            val phone = inputPhone.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.toastShort("请输入手机号")
                return@setOnClickListener
            }
        }
    }

    /**
     * 注册LiveData的观察者
     */
    private fun registerLiveDataObserver() {

    }

}