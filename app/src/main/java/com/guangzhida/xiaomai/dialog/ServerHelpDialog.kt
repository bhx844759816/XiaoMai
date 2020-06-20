package com.guangzhida.xiaomai.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.LogUtils

/**
 * 客服帮助列表
 */

class ServerHelpDialog : DialogFragment() {
    private lateinit var mViewModel: ServerHelpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(ServerHelpViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = activity?.run {
            val dialog = MaterialDialog(this)
                .title(text = "提示")
                .input(hint = "请输入校园网账号", inputType = InputType.TYPE_CLASS_PHONE)
                .cornerRadius(res = R.dimen.dialog_corner_radius)
                .negativeButton {

                }
                .positiveButton {

                }
                .show {

                }
            dialog
        }

        return dialog ?: super.onCreateDialog(savedInstanceState)
    }


    companion object {
        private val TAG = ServerHelpDialog::class.simpleName

        fun showDialog(activity: FragmentActivity?) {
            var fragment = activity?.supportFragmentManager?.findFragmentByTag(TAG)
            if (fragment == null) {
                val dialog = ServerHelpDialog()
                fragment = dialog
            }
            if (!fragment.isAdded) {
                val manager = activity?.supportFragmentManager
                val transaction = manager?.beginTransaction()
                transaction?.add(fragment, TAG)
                transaction?.commitAllowingStateLoss()
            }
        }

        fun dismissDialog(activity: FragmentActivity?) {
            val fragment = activity?.supportFragmentManager?.findFragmentByTag(TAG)
            if (fragment != null && fragment is ServerHelpDialog) {
                fragment.dismissAllowingStateLoss()
            }
        }
    }
}

