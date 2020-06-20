package com.guangzhida.xiaomai.base

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.event.Message
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.utils.StatusBarTextUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.jaeger.library.StatusBarUtil
import java.lang.reflect.ParameterizedType

@SuppressLint("Registered")
abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {
    lateinit var mViewModel: VM
    private var dialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
        StatusBarTextUtils.setLightStatusBar(this, true)
        createViewModel()
        initView(savedInstanceState)
        initListener()
        registerUIChange()
    }


    open fun initView(savedInstanceState: Bundle?) {

    }

    open fun initListener() {

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
        mViewModel.defUI.msgEvent.observe(this, Observer {
            handleEvent(it)
        })
        LiveDataBus.with(LiveDataBusKey.IM_KICKED_BY_OTHER_DEVICE).observe(this, Observer {
            showImLoginOtherDeviceDialog()
        })
    }

    open fun handleEvent(msg: Message) {}

    /*
    * 布局ID
    * */
    abstract fun layoutId(): Int

    /**
     * 打开等待框
     */
    private fun showLoading() {
        if (dialog == null) {
            dialog = MaterialDialog(this)
                .cancelable(false)
                .cornerRadius(8f)
                .customView(R.layout.custom_progress_dialog_view, noVerticalPadding = true)
                .lifecycleOwner(this)
                .maxWidth(R.dimen.dialog_loading_width)
        }
        dialog?.show()

    }

    /**
     * 展示异地和其他设备登录Im问题
     */
    fun showImLoginOtherDeviceDialog() {
        MaterialDialog(this)
            .cancelable(false)
            .cornerRadius(8f)
            .customView(R.layout.dialog_other_device_login_remind_layout, dialogWrapContent = true,noVerticalPadding = true)
            .show {
                this.getCustomView().findViewById<TextView>(R.id.tvCancel).clickN {
                    this.dismiss()
                }
                this.getCustomView().findViewById<TextView>(R.id.tvConfirm).clickN {
                    this.dismiss()
                    startKtxActivity<LoginActivity>()
                }
            }
    }

    /**
     * 关闭等待框
     */
    private fun dismissLoading() {
        dialog?.run { if (isShowing) dismiss() }
    }


    /**
     * 创建 ViewModel
     */
    @Suppress("UNCHECKED_CAST")
    private fun createViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val tp = type.actualTypeArguments[0]
            val tClass = tp as? Class<VM> ?: BaseViewModel::class.java

            mViewModel = ViewModelProvider(this).get(tClass) as VM

        }
    }

    fun checkPermission(permissions: List<String>): Boolean {
        permissions.forEach {
            val per = ContextCompat.checkSelfPermission(this, it);
            if (PackageManager.PERMISSION_GRANTED != per) {
                return false;
            }
        }
        return true;
    }

    fun getStatusBarHeight(): Int {
        val resourceId: Int =
            resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
}