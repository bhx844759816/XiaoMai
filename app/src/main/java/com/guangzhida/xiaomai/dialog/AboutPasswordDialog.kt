package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems
import com.guangzhida.xiaomai.R

/**
 *
 */
object AboutPasswordDialog {
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callback: ((Int) -> Unit)?
    ) {
        MaterialDialog(context)

            .cornerRadius(res = R.dimen.dialog_corner_radius)
            .customView(viewRes = R.layout.dialog_about_password_layout)

            .maxWidth(res = R.dimen.dialog_width)
            .lifecycleOwner(owner)
            .show {
                val modifyPassword = getCustomView().findViewById<TextView>(R.id.tvModifyPassword)
                val forgetPassword = getCustomView().findViewById<TextView>(R.id.tvForgetPassword)
                //修改密码
                modifyPassword.setOnClickListener {
                    callback?.invoke(0)
                    dismiss()
                }
                //忘记密码
                forgetPassword.setOnClickListener {
                    callback?.invoke(1)
                    dismiss()
                }
            }
    }
}