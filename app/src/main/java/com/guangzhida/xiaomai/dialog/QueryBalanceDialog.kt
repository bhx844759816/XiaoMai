package com.guangzhida.xiaomai.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.model.AccountModel

/**
 * 余额查询的Dialog
 */
object QueryBalanceDialog {
    /**
     *
     */
    fun showDialog(context: Context, owner: LifecycleOwner, accountModel: AccountModel) {
        val message = buildString {
            append("账号余额： ")
            append(accountModel.money)
            append("\n")
            append("所属套餐： ")
            append(accountModel.servername)
            append("\n")
            append("开通时间： ")
            append(accountModel.registrtime)
            append("\n")
            append("到期时间： ")
            append(accountModel.expiretime)
        }
        MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .title(text = "账户信息")
            .message(text = message)
            .lifecycleOwner(owner)
            .show()

    }
}