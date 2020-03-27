package com.guangzhida.xiaomai.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.dialog.MaterialDialogs
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.ToastUtils

object BindAccountDialog {
    private var dialog: MaterialDialog? = null
    /**
     * 展示对话框
     */
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callBack: ((String, String) -> Unit)? = null
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_bind_account_layout, null)
        val bindBtn = view.findViewById<TextView>(R.id.tvBindAccount)
        val inputPhone = view.findViewById<EditText>(R.id.etInputAccount)
        val inputPassword = view.findViewById<EditText>(R.id.etInputPassword)
        bindBtn.setOnClickListener {
            val phone = inputPhone.text.toString().trim()
            val password = inputPassword.text.toString().trim()
            if (phone.isEmpty()) {
                ToastUtils.toastShort("请输入手机号")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                ToastUtils.toastShort("请输入密码")
                return@setOnClickListener
            }
            callBack?.invoke(phone, password)
        }
        MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .customView(view = view, noVerticalPadding = true)
            .lifecycleOwner(owner)
            .show {
                dialog = this
            }

    }

    fun dismissDialog() {
        dialog?.dismiss()
    }
}

