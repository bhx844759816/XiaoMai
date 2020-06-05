package com.guangzhida.xiaomai.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.isPhone
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.ui.login.RegisterActivity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 当绑定过校园卡的时候 未登录的情况选择登录方式
 */
class SchoolPhoneAccountLoginDialog : DialogFragment() {
    private lateinit var mViewModel: SchoolPhoneAccountLoginViewModel
    private val mAccountModel = BaseApplication.instance().mAccountModel
    private var dialog: MaterialDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.i("SchoolPhoneAccountLoginDialog oncreate ")
        mViewModel = ViewModelProvider(this).get(SchoolPhoneAccountLoginViewModel::class.java)
        registerUIChange()
        //发送验证码成功
        mViewModel.mSmsCodeLiveData.observe(this, Observer {
            if (it) {
                PhoneVerificationDialog.sendSmsCodeSuccess()
            }
        })
        mViewModel.mBindSchoolAccountLoginResult.observe(this, Observer {
            userModelChangeLiveData.postValue(true)
            PhoneVerificationDialog.dismissDialog()
            activity?.let { dismissDialog(it) }
        })
        //用户已经注册提示用户去登录
        mViewModel.mUserExistObserver.observe(this, Observer {
            showLoginPromptDialog()
        })
        LiveDataBus.with(LiveDataBusKey.LOGIN_KEY, Boolean::class.java).observe(this, Observer {
            PhoneVerificationDialog.dismissDialog()
            activity?.let {
                dismissDialog(it)
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.run {
            val dialog = MaterialDialog(this)
                .cornerRadius(res = R.dimen.dialog_corner_radius)
                .customView(viewRes = R.layout.dialog_bind_school_account_layout)
                .maxWidth(res = R.dimen.dialog_width)
                .show {
                    val tvSchoolAccountLogin =
                        getCustomView().findViewById<TextView>(R.id.tvSchoolAccountLogin)
                    val tvNewPhoneLogin =
                        getCustomView().findViewById<TextView>(R.id.tvNewPhoneLogin)
                    //校园卡登录
                    tvSchoolAccountLogin.setOnClickListener {
                        doLoginBySchoolAccount()
                    }
                    //新手机号注册登录
                    tvNewPhoneLogin.setOnClickListener {
                        doLoginByPhone()
                    }
                }
            dialog
        }
        return dialog ?: super.onCreateDialog(savedInstanceState)
    }

    /**
     * 登录通过校园卡
     */
    private fun doLoginBySchoolAccount() {
        activity?.let {
            val phone = if (mAccountModel?.user?.isPhone() == true) {
                mAccountModel?.user
            } else {
                null
            }
            PhoneVerificationDialog.showDialog(it, it, phone, sendSmsCodeCallBack = fun(phone) {
                LogUtils.i("sendSmsCodeCallBack callback")
                mViewModel.sendSmsCode(phone)
            }, loginCallBack = fun(phone, smsCode) {
                LogUtils.i("loginCallBack callback")
                mViewModel.registerAndLoginBySchoolAccount(
                    phone,
                    mAccountModel?.pass ?: "",
                    smsCode,
                    mAccountModel?.user ?: "",
                    mAccountModel?.pass ?: ""
                )
            })
        }
    }

    /**
     * 通过新手机号登录
     */
    private fun doLoginByPhone() {
        startKtxActivity<RegisterActivity>(
            values = listOf(
                Pair(
                    "SchoolAccount",
                    BaseApplication.instance().mAccountModel?.user ?: ""
                ), Pair(
                    "SchoolPassword",
                    BaseApplication.instance().mAccountModel?.pass ?: ""
                ),
                Pair("RegisterType", 1)
            )
        )
    }

    /**
     * 注册 UI 事件
     */
    private fun registerUIChange() {
        mViewModel.defUI.showDialog.observe(this, Observer {
            showLoading()
        })
        mViewModel.defUI.dismissDialog.observe(this, Observer {
            dismissLoading()
        })
        mViewModel.defUI.toastEvent.observe(this, Observer {
            ToastUtils.toastShort(it)
        })
    }

    /**
     * 打开等待框
     */
    private fun showLoading() {
        if (dialog == null) {
            context?.let {
                dialog = MaterialDialog(it)
                    .cancelable(false)
                    .cornerRadius(8f)
                    .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
                    .lifecycleOwner(this)
                    .maxWidth(R.dimen.dialog_loading_width)
            }
        }
        dialog?.show()

    }

    /**
     * 提示登录对话框
     */
    private fun showLoginPromptDialog() {
        context?.let {
            MaterialDialog(it)
                .cancelable(false)
                .cornerRadius(8f)
                .title(text = "提示")
                .message(text = "账号已注册是否跳转到登录界面")
                .positiveButton(text = "确定") {
                    startKtxActivity<LoginActivity>()
                }
                .negativeButton(text = "取消") {
                    it.dismiss()
                }
                .lifecycleOwner(this)
                .show()
        }
    }

    /**
     * 关闭等待框
     */
    private fun dismissLoading() {
        dialog?.run { if (isShowing) dismiss() }
    }

    companion object {
        private val TAG = SchoolPhoneAccountLoginDialog::class.simpleName

        fun showDialog(activity: FragmentActivity?) {
            LogUtils.i("SchoolPhoneAccountLoginDialog showDialog")
            var fragment = activity?.supportFragmentManager?.findFragmentByTag(TAG)
            if (fragment == null) {
                val dialog = SchoolPhoneAccountLoginDialog()
                fragment = dialog
            }
            if (!fragment.isAdded) {
                val manager = activity?.supportFragmentManager
                val transaction = manager?.beginTransaction()
                transaction?.add(fragment, TAG)
                transaction?.commitAllowingStateLoss()
            }
        }

        fun dismissDialog(activity: FragmentActivity?) {
            val fragment =
                activity?.supportFragmentManager?.findFragmentByTag(TAG)
            if (fragment != null && fragment is SchoolPhoneAccountLoginDialog) {
                fragment.dismissAllowingStateLoss()
            }
        }
    }
}