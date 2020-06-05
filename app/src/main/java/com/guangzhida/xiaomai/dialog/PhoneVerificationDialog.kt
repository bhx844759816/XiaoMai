package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.isPhone
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import org.apache.commons.logging.Log

/**
 * 手机验证码登录绑定校园卡
 */
object PhoneVerificationDialog {
    var tvSendSmsCode: TextView? = null
    private var mTimer: MyCountDownTimer? = null
    private var mDialog:MaterialDialog?= null
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        schoolPhoneAccount: String?,
        sendSmsCodeCallBack: ((String) -> Unit)?,
        loginCallBack: ((String, String) -> Unit)?//登录的回调 第一个参数表示手机号 第二个参数表示验证码
    ) {
        mDialog =  MaterialDialog(context)
            .cornerRadius(res = R.dimen.dialog_corner_radius)
            .customView(viewRes = R.layout.dialog_phone_verification_login_layout)
            .show {
                val etInputPhone = getCustomView().findViewById<EditText>(R.id.etInputPhone)
                val etInputCode = getCustomView().findViewById<EditText>(R.id.etInputCode)
                tvSendSmsCode = getCustomView().findViewById(R.id.tvSendSmsCode)
                val tvProtocol = getCustomView().findViewById<TextView>(R.id.tvProtocol)
                val tvLogin = getCustomView().findViewById<TextView>(R.id.tvLogin)
                etInputPhone.setText(schoolPhoneAccount ?: "")
                etInputPhone.setSelection(schoolPhoneAccount?.length ?: 0)
                KtxSpan().with(tvProtocol)
                    .text("登陆即表示同意", isNewLine = false)
                    .text(
                        "<<用户服务协议>>",
                        foregroundColor = Color.parseColor("#9245ec"),
                        isNewLine = false
                    )
                    .show { }
                //点击用户服务协议
                tvProtocol.setOnClickListener {
                    context.startKtxActivity<WebActivity>(
                        values = listOf(
                            Pair("url", "file:///android_asset/ServiceAgreement.html"),
                            Pair("type", "protocol")
                        )
                    )
                }
                //点击发送验证码
                tvSendSmsCode?.clickN {
                    LogUtils.i("tvSendSmsCode click")
                    val phone = etInputPhone.text.toString().trim()
                    if (phone.isEmpty() || !phone.isPhone()) {
                        ToastUtils.toastShort("手机号不合法")
                        return@clickN
                    }
                    sendSmsCodeCallBack?.invoke(phone)
                }
                //登录的回调
                tvLogin.clickN {
                    LogUtils.i("tvLogin click")
                    val phone = etInputPhone.text.toString().trim()
                    val smsCode = etInputCode.text.toString().trim()
                    if (phone.isEmpty() || !phone.isPhone()) {
                        ToastUtils.toastShort("手机号不合法")
                        return@clickN
                    }
                    if (smsCode.isEmpty()) {
                        ToastUtils.toastShort("验证码不能为空")
                        return@clickN
                    }
                    loginCallBack?.invoke(phone, smsCode)
                }

            }
    }

    fun dismissDialog(){
        mDialog?.dismiss()
    }

    fun sendSmsCodeSuccess() {
        mTimer = MyCountDownTimer(60000, 1000)
        mTimer?.start()

    }

    //倒计时函数
    class MyCountDownTimer(
        millisInFuture: Long,
        countDownInterval: Long
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(l: Long) { //防止计时过程中重复点击
            tvSendSmsCode?.isEnabled = false
            tvSendSmsCode?.text = buildString {
                append(l / 1000)
                append("秒")
            }
        }

        override fun onFinish() { //重新给Button设置文字
            tvSendSmsCode?.text = "重新获取"
            //设置可点击
            tvSendSmsCode?.isEnabled = true
        }
    }

}