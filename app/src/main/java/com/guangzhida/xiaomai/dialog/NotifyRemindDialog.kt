package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.ext.goToAppInfoPage

/**
 * 提示打开通知栏的Dialog
 */
object NotifyRemindDialog {
    /**
     * 展示通知提示的Dialog
     */
    fun showDialog(context: Context, owner: LifecycleOwner) {
        MaterialDialog(context)
            .cancelable(false)
            .customView(viewRes = R.layout.dialog_notify_remind_layout)
            .lifecycleOwner(owner)
            .show {
                val view = getCustomView()
                view.findViewById<TextView>(R.id.tvCancel).setOnClickListener {
                    this.dismiss()
                }
                view.findViewById<TextView>(R.id.tvConfirm).setOnClickListener {
                    context.goToAppInfoPage()
                    this.dismiss()
                }
            }
    }

}