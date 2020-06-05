package com.guangzhida.xiaomai.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.RegisterViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_register_layout.*


/**
 * 注册界面
 */
class RegisterActivity : BaseActivity<RegisterViewModel>() {

    private var mTimer: MyCountDownTimer? = null
    private var mSchoolAccount: String? = null //校园卡账号
    private var mSchoolPassword: String? = null//校园卡密码
    private var mRegisterType = 0 //注册类型当是从绑定校园卡过来的时候注册成功跳转到登录界面
    override fun layoutId(): Int = R.layout.activity_register_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mSchoolAccount = intent.getStringExtra("SchoolAccount")
        mSchoolPassword = intent.getStringExtra("SchoolPassword")
        mRegisterType = intent.getIntExtra("RegisterType", 0)
        KtxSpan().with(tvProtocol)
            .text("已阅读并同意", isNewLine = false)
            .text("<<用户服务协议>>", foregroundColor = Color.parseColor("#9245ec"), isNewLine = false)
            .show { }
        //点击用户服务协议
        tvProtocol.setOnClickListener {
            startKtxActivity<WebActivity>(
                values = listOf(
                    Pair("url", "file:///android_asset/ServiceAgreement.html"),
                    Pair("type", "protocol")
                )
            )
        }
        idCancel.setOnClickListener { finish() }
        registerLiveDataObserver()
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
            val code = inputCode.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.toastShort("请输入手机号")
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                ToastUtils.toastShort("请输入验证码")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                ToastUtils.toastShort("请输入密码")
                return@setOnClickListener
            }
            if (password.length < 6 || password.length > 20) {
                ToastUtils.toastShort("密码至少6位最多不超过20")
                return@setOnClickListener
            }
            if(!cbProtocol.isChecked){
                ToastUtils.toastShort("请阅读并同意用户协议")
                return@setOnClickListener
            }
            //注册
            mViewModel.register(phone, code, password, mSchoolAccount ?: "", mSchoolPassword ?: "")
        }
    }

    /**
     * 注册LiveData的观察者
     */
    private fun registerLiveDataObserver() {
        //注册结果回调
        mViewModel.mRegisterResultLiveData.observe(this, Observer {
            if (it) {
                ToastUtils.toastShort("注册成功")
                if (mRegisterType == 1) {
                    startKtxActivity<LoginActivity>()
                }
                finish()
            }
        })
        //发送验证码结果回调
        mViewModel.mSmsCodeLiveData.observe(this, Observer {
            if (it) {
                mTimer = MyCountDownTimer(60000, 1000)
                mTimer?.start()
            }
        })
    }


    //倒计时函数
    inner class MyCountDownTimer(
        millisInFuture: Long,
        countDownInterval: Long
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(l: Long) { //防止计时过程中重复点击
            tvSendSmsCode.isClickable = false
            tvSendSmsCode.text = buildString {
                append(l / 1000)
                append("秒")
            }
        }

        override fun onFinish() { //重新给Button设置文字
            tvSendSmsCode.text = "重新获取"
            //设置可点击
            tvSendSmsCode.isClickable = true
        }
    }

}