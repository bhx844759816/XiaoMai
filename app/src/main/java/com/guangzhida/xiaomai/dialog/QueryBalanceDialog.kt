package com.guangzhida.xiaomai.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner

/**
 * 余额查询的Dialog
 */
object QueryBalanceDialog {
    /**
     *
     */
    fun showDialog(context: Context,owner:LifecycleOwner) {
        MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .title(text = "账户信息")
            .message(text = "余额100" )
            .lifecycleOwner(owner)
            .show()

    }
}