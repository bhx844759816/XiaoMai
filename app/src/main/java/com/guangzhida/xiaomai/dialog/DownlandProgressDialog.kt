package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.guangzhida.xiaomai.R

/**
 * 下载进度对话框
 */
object DownlandProgressDialog {
    private var mProgressBar: ProgressBar? = null
    private var mProgressText: TextView? = null

    fun showDialog(context: Context) {
        MaterialDialog(context)
            .cancelable(false)
            .customView(viewRes = R.layout.dialog_downland_progress_layout)
            .show {
                val view = getCustomView()
                mProgressBar = view.findViewById(R.id.pbProgress)
                mProgressText = view.findViewById(R.id.tvProgress)
            }
    }

    /**
     * 改变进度
     */
    fun changeProgress(progress: Int) {
        mProgressBar?.progress = progress
        mProgressText?.text = buildString {
            append(progress)
            append("%")
        }
    }

}