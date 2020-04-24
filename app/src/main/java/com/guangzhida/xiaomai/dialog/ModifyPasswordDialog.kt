package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 修改密码
 */
object ModifyPasswordDialog {
    private var dialog: MaterialDialog? = null
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callBack: ((oldPassword: String, newPassword: String) -> Unit)?
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_modify_password_layout, null)
        val etOldPassword = view.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmNewPassword = view.findViewById<EditText>(R.id.etConfirmNewPassword)
        val tvConfirmModify = view.findViewById<TextView>(R.id.tvConfirmModify)
        tvConfirmModify.setOnClickListener {
            val oldPassword = etOldPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmNewPassword.text.toString().trim()
            if (oldPassword.isEmpty()) {
                ToastUtils.toastShort("请输入原密码")
                return@setOnClickListener
            }
            if (newPassword.isEmpty()) {
                ToastUtils.toastShort("请输入新密码")
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                ToastUtils.toastShort("请输入确认新密码")
                return@setOnClickListener
            }
            if (confirmPassword != newPassword) {
                ToastUtils.toastShort("确认新密码和新密码不一致")
                return@setOnClickListener
            }
            callBack?.invoke(oldPassword, newPassword)
            dialog?.dismiss()
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