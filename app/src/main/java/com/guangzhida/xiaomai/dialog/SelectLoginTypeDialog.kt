package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R

/**
 * 选择登录方式
 */
object SelectLoginTypeDialog {

    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callback: ((Int) -> Unit)?
    ) {
        MaterialDialog(context)
            .cornerRadius(res = R.dimen.dialog_corner_radius)
            .cancelOnTouchOutside(false)
            .customView(viewRes = R.layout.dialog_bind_school_account_layout)
            .maxWidth(res = R.dimen.dialog_width)
            .lifecycleOwner(owner)
            .show {
                val tvSchoolAccountLogin =
                    getCustomView().findViewById<TextView>(R.id.tvSchoolAccountLogin)
                val tvNewPhoneLogin = getCustomView().findViewById<TextView>(R.id.tvNewPhoneLogin)
                //校园卡登录
                tvSchoolAccountLogin.setOnClickListener {
                    callback?.invoke(0)
                    dismiss()
                }
                //新手机号注册登录
                tvNewPhoneLogin.setOnClickListener {
                    callback?.invoke(1)
                    dismiss()
                }
            }
    }

}