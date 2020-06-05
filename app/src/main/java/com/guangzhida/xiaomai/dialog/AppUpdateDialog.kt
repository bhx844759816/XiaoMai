package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R

/**
 * 提示更像dialog
 */
object AppUpdateDialog {


    /**
     * 展示通知提示的Dialog
     */
    fun showDialog(context: Context,content:String,callback:()->Unit) {
        MaterialDialog(context)
            .cancelable(false)
            .customView(viewRes = R.layout.dialog_app_update_layout)
            .show {
                val view = getCustomView()
                val tvContent = view.findViewById<TextView>(R.id.tvContent)
                tvContent.text=content
                tvContent.movementMethod = ScrollingMovementMethod()
                view.findViewById<TextView>(R.id.tvDoUpdate).setOnClickListener {
                    callback.invoke()
                    dismiss()
                }
            }
    }

}