package com.guangzhida.xiaomai.ui.user

import android.os.Bundle
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.appName
import com.guangzhida.xiaomai.ktxlibrary.ext.versionName
import com.guangzhida.xiaomai.ui.user.viewmodel.AboutUsViewModel
import kotlinx.android.synthetic.main.activity_about_us.*

class AboutUsActivity : BaseActivity<AboutUsViewModel>() {
    override fun layoutId(): Int = R.layout.activity_about_us


    override fun initView(savedInstanceState: Bundle?) {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        tvAppName.text = appName
        //
        tvContent.text = buildString {
            append("客服热线： 0371-2299-3454\n")
            append("客服邮箱： zzguangzhida@126.com\n")
            append("当前版本： ")
            append(versionName)
        }
        //
        tvDescribe.text = buildString {
            append("校麦是一款专业快速连接校园网\n")
            append("丰富校园美好生活的app\n")
            append("欢迎加入校麦")
        }
    }
}