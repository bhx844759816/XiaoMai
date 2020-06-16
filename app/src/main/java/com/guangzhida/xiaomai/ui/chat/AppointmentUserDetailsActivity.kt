package com.guangzhida.xiaomai.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.ui.chat.adapter.AppointmentSingUpAdapter
import com.guangzhida.xiaomai.ui.chat.adapter.ImageBannerAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AppointmentUserDetailsViewModel
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.jaeger.library.StatusBarUtil
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.activity_appointment_user_details_layout.*

/**
 * 自己发布的详情页面
 */
class AppointmentUserDetailsActivity : BaseActivity<AppointmentUserDetailsViewModel>() {
    private lateinit var mAppointmentModel: AppointmentModel
    private val mImageList = mutableListOf<String>()
    private val mUserList = mutableListOf<ChatUserModel>()
    private val mImageBannerAdapter = ImageBannerAdapter(mImageList)
    private val mUserAdapter = AppointmentSingUpAdapter(mUserList)

    private val mGson by lazy {
        Gson()
    }


    override fun layoutId(): Int = R.layout.activity_appointment_user_details_layout


    override fun initView(savedInstanceState: Bundle?) {
        StatusBarUtil.setTranslucentForImageView(this, 0, toolBar)
        mAppointmentModel = mGson.fromJson<AppointmentModel>(
            intent.getStringExtra("appointmentModel"),
            AppointmentModel::class.java
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mUserAdapter
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
        initData()
        mViewModel.getSignUpUserList(mAppointmentModel.id.toString())
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
        ivUserAvatar.loadCircleImage(
            BASE_URL_IMG + BaseApplication.instance().mUserModel?.headUrl,
            holder = R.mipmap.icon_user_center_header
        )
        tvUserName.text = BaseApplication.instance().mUserModel?.name

    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        tvSeeMore.clickN {
            startKtxActivity<AllSingUpUserActivity>(
                value = Pair(
                    "activityId",
                    mAppointmentModel.id.toString()
                )
            )
        }
        mUserAdapter.mContactCallBack = {
            val params = Pair("userName", it.mobilePhone)
            startKtxActivity<ChatMessageActivity>(value = params)
        }
        //获取所有报名的用户对象
        mViewModel.mChatUserModelObserver.observe(this, Observer {
            if (it.isEmpty()) {
                llUserSignUpParent.gone()
            } else {
                llUserSignUpParent.visible()
                if (it.size > 3) {
                    val list = it.subList(0, 3)
                    mUserList.addAll(list)
                } else {
                    mUserList.addAll(it)
                }
                tvSeeMore.text = buildString {
                    append("查看更多(")
                    append(it.size)
                    append(") >>")
                }
                mUserAdapter.notifyDataSetChanged()
            }
        })
    }
}