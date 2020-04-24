package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.viewmodel.SettingRemarkViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_setting_remark_layout.*

/**
 * 设置备注
 */
class SettingRemarkActivity : BaseActivity<SettingRemarkViewModel>() {
    private var mUserEntity: UserEntity? = null
    private val mGson by lazy {
        Gson()
    }

    override fun layoutId(): Int = R.layout.activity_setting_remark_layout

    override fun initView(savedInstanceState: Bundle?) {
        val userEntityGson = intent.getStringExtra("UserEntityGson")
        mUserEntity = mGson.fromJson(userEntityGson, UserEntity::class.java)
        etInputRemarkName.setText(mUserEntity?.remarkName)
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        //保存
        tvSave.clickN {
            val remarkName = etInputRemarkName.text.toString().trim()
            if (remarkName.isEmpty()) {
                ToastUtils.toastShort("请输入备注名")
                return@clickN
            }
            mUserEntity?.let {
                it.remarkName = remarkName
                mViewModel.modifyRemark(it)
            }
        }
        //修改备注的结果
        mViewModel.modifyRemarkResult.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }
}