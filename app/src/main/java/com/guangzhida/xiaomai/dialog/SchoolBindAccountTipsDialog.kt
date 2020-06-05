package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN

/**
 * 当账号的学校和用户选择的校区不一致的时候提示的对话框
 */
object SchoolBindAccountTipsDialog {


    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        content: String,
        callBack: (() -> Unit)? = null
    ) {
        MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .customView(
                viewRes = R.layout.dialog_school_bind_account_tips_layout,
                noVerticalPadding = true,
                dialogWrapContent = true,
                horizontalPadding = false
            )
            .lifecycleOwner(owner)
            .maxWidth(R.dimen.dialog_width_big)
            .show {
                val tvContent = getCustomView().findViewById<TextView>(R.id.tvContent)
                val tvConfirm = getCustomView().findViewById<TextView>(R.id.tvConfirm)
                tvContent.text = content
                tvConfirm.clickN {
                    callBack?.invoke()
                    dismiss()
                }
            }
    }
}