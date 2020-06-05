package com.guangzhida.xiaomai.ui

import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import com.guangzhida.xiaomai.utils.ToastUtils


class AndroidInterface constructor(activity: FragmentActivity) {
    private val mActivity = activity
    @JavascriptInterface
    fun toast(msg: String) {
        ToastUtils.ioToastShort(msg)
    }

    @JavascriptInterface
    fun finish() {
        mActivity.finish()
    }
}