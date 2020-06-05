package com.guangzhida.xiaomai.ext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.dialog.SchoolPhoneAccountLoginDialog
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.login.LoginActivity

/**
 * 未登录时 判断当前的状态显示不同的登录界面
 */
internal fun FragmentActivity.jumpLoginByState() {
    val mAccountModel = BaseApplication.instance().mAccountModel
    //未绑定的时候跳转到
    if (mAccountModel == null) {
        startKtxActivity<LoginActivity>()
    } else {
        SchoolPhoneAccountLoginDialog.showDialog(this)
    }
}
