package com.guangzhida.xiaomai.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.formatDateTime
import com.guangzhida.xiaomai.ktxlibrary.ext.notNull
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.ui.chat.adapter.ImageBannerAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AppointmentDetailsViewModel
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.jaeger.library.StatusBarUtil
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.activity_appointment_details_layout.*

/**
 * 约吗的详情页面
 */
class AppointmentDetailsActivity : BaseActivity<AppointmentDetailsViewModel>() {
    private lateinit var mAppointmentModel: AppointmentModel
    private val mImageList = mutableListOf<String>()
    private val mImageBannerAdapter = ImageBannerAdapter(mImageList)
    private val mGson by lazy {
        Gson()
    }
    private var mUserModel: UserModel.Data? = null

    override fun layoutId(): Int = R.layout.activity_appointment_details_layout

    override fun initView(savedInstanceState: Bundle?) {
        StatusBarUtil.setTranslucentForImageView(this, 0, toolBar)
        mAppointmentModel = mGson.fromJson<AppointmentModel>(
            intent.getStringExtra("appointmentModel"),
            AppointmentModel::class.java
        )
        banner.addBannerLifecycleObserver(this)//添加生命周期观察者
            .setAdapter(mImageBannerAdapter)
            .setIndicator(CircleIndicator(this))
            .setOnBannerListener { _, position ->
                val intent = Intent(
                    this,
                    PreviewResultListActivity::class.java
                )
                val mPhotoList = arrayListOf<String>()
                mImageList.forEach {
                    mPhotoList.add(BASE_URL_IMG + it)
                }
                intent.putStringArrayListExtra("imgUrls", mPhotoList)
                intent.putExtra("pos", position)
                startActivity(intent)
            }
            .start();
        mViewModel.getUserInfo(mAppointmentModel.userId.toString())
        //通过用户id获取用户的头像
        initData()
    }

    private fun initData() {
        val photoList = mAppointmentModel.activityPic.split(",")
        mImageList.addAll(photoList)
        mImageBannerAdapter.notifyDataSetChanged()
        tvTitle.text = mAppointmentModel.title
        tvDec.text = mAppointmentModel.content
        tvActivityDetails.text = buildString {
            append(mAppointmentModel.activityStartTime.formatDateTime())
            append("\n")
            append(mAppointmentModel.activityAddress)
            append("\n")
            append("参与人数: ")
            append(mAppointmentModel.boyCount)
            append("男,")
            append(mAppointmentModel.girlCount)
            append("女")
        }
        tvSingUp.isEnabled = mAppointmentModel.isExpire == 0 && mAppointmentModel.isSign == 0
        if (mAppointmentModel.isExpire != 0) {
            tvSingUp.text = "已过期"
        } else if (mAppointmentModel.isSign != 0) {
            tvSingUp.text = "已报名"
        }
        tvActivitySignUpEndTime.text = mAppointmentModel.signEndTime.formatDateTime()
        if (mAppointmentModel.feeType == 0) {
            tvMoney.text = "免费"
        } else {
            val type = when (mAppointmentModel.feeType) {
                1 -> {
                    "(A费)"
                }
                2 -> {
                    "(男)"
                }
                3 -> {
                    "(女)"

                }
                else -> "(A费)"
            }
            KtxSpan().with(tvMoney).text("$${mAppointmentModel.activityMoney}", isNewLine = false)
                .text(type, isNewLine = false, textSize = 12).show { }
        }
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        //联系他
        tvContact.clickN {
            if (mUserModel != null) {
                val params = Pair("userName", mUserModel!!.mobilePhone)
                startKtxActivity<ChatMessageActivity>(value = params)
            }
        }
        //报名参加
        tvSingUp.clickN {
            mViewModel.singUpActivity(mAppointmentModel.id.toString())
        }
        //获取到用户信息
        mViewModel.mUserInfoObserver.observe(this, Observer {
            mUserModel = it
            ivUserAvatar.loadCircleImage(
                BASE_URL_IMG + "/" + it.headUrl,
                holder = R.mipmap.icon_user_center_header
            )
            tvUserName.text = it.nickName
        })

        mViewModel.mSingUpResultObserver.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }


}