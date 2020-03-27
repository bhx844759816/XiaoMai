package com.guangzhida.xiaomai.ui.home

import android.os.Bundle
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ui.home.viewmodel.MessageCenterViewModel
import kotlinx.android.synthetic.main.activity_message_center.*

/**
 * 消息中心
 */
class MessageCenterActivity : BaseActivity<MessageCenterViewModel>() {
    override fun layoutId(): Int = R.layout.activity_message_center

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initListener() {
        idToolBar.setNavigationOnClickListener {
            finish()
        }
    }
}