package com.guangzhida.xiaomai.dialog

import android.app.Activity
import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 修改套餐提示框
 */
object ModifyAccountTipsDialog {


    fun showDialog(context: Activity, owner: LifecycleOwner, callBack: (() -> Unit)? = null) {
        MaterialDialog(context)
            .cornerRadius(8f)
            .customView(
                viewRes = R.layout.dialog_modify_account_tips_layout,
                noVerticalPadding = true,
                dialogWrapContent = true,
                horizontalPadding = false
            )
            .lifecycleOwner(owner)
            .maxWidth(R.dimen.dialog_width_big)
            .show {
                val cbConfirm = getCustomView().findViewById<CheckBox>(R.id.cbConfirm)
                val tvConfirm = getCustomView().findViewById<TextView>(R.id.tvConfirm)
                tvConfirm.clickN {
                    if (!cbConfirm.isChecked) {
                        ToastUtils.toastShort("请勾选同意清除套餐")
                        return@clickN
                    }
                    callBack?.invoke()
                    dismiss()
                }
            }

    }

}