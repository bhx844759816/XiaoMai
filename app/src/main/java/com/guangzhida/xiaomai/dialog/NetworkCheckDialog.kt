package com.guangzhida.xiaomai.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner

/**
 * 网络诊断Dialog
 */
object NetworkCheckDialog {
    private var dialog: MaterialDialog? = null
    /**
     *
     */
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        content: String,
        dismissCallBack: (() -> Unit)? = null
    ) {
        dialog = MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .title(text = "网络诊断")
            .message(text = content)
            .lifecycleOwner(owner)
        dialog?.setOnDismissListener {
            dismissCallBack?.invoke()
        }
        dialog?.show()

    }

    fun changeDialogMessage(message: String) {
        dialog?.message(text = message)
    }
}