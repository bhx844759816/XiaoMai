package com.guangzhida.xiaomai.ui.appointment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentSingUpAdapter
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentCarDetailsFragment
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentPlayDetailsFragment
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentPublishPlayFragment
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentWorkDetailsFragment
import com.guangzhida.xiaomai.ui.chat.adapter.ImageBannerAdapter
import com.guangzhida.xiaomai.ui.appointment.viewmodel.AppointmentDetailsViewModel
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
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
    private val mUserList = mutableListOf<ChatUserModel>()
    private val mUserAdapter =
        AppointmentSingUpAdapter(
            mUserList
        )
    private var mFragment: Fragment? = null
    private var mUserModel: UserModel.Data? = null

    override fun layoutId(): Int = R.layout.activity_appointment_details_layout

    override fun initView(savedInstanceState: Bundle?) {
        StatusBarUtil.setTranslucentForImageView(this, 0, toolBar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = mUserAdapter
        mAppointmentModel = mGson.fromJson<AppointmentModel>(
            intent.getStringExtra("appointmentModel"),
            AppointmentModel::class.java
        )
        addFragment()
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
        mViewModel.getSignUpUserList(mAppointmentModel.id.toString())
        //通过用户id获取用户的头像
        initData()
    }

    private fun initData() {
        val photoList =
            if (mAppointmentModel.activityPic != null && mAppointmentModel.activityPic!!.isNotEmpty()) {
                mAppointmentModel.activityPic!!.split(",")
            } else {
                listOf()
            }
        mImageList.addAll(photoList)
        mImageBannerAdapter.notifyDataSetChanged()
        if (mAppointmentModel.userId.toString() != BaseApplication.instance().mUserModel!!.id) {
            llBottomControl.visible()
            tvSingUp.isEnabled = mAppointmentModel.isExpire == 0 && mAppointmentModel.isSign == 0
            if (mAppointmentModel.isExpire != 0) {
                tvSingUp.text = "已过期"
            } else if (mAppointmentModel.isSign != 0) {
                tvSingUp.text = "已报名"
            }
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
                KtxSpan().with(tvMoney)
                    .text("$${mAppointmentModel.activityMoney}", isNewLine = false)
                    .text(type, isNewLine = false, textSize = 12).show { }
            }
        }

    }

    /**
     * 填充fragment
     */
    private fun addFragment() {
        mFragment = when (mAppointmentModel.type) {
            1 -> {
                AppointmentPlayDetailsFragment.newInstance(mAppointmentModel)
            }
            2 -> {
                AppointmentCarDetailsFragment.newInstance(mAppointmentModel)
            }
            3 -> {
                AppointmentWorkDetailsFragment.newInstance(mAppointmentModel)
            }
            else -> {
                AppointmentPlayDetailsFragment.newInstance(mAppointmentModel)
            }
        }
        supportFragmentManager.beginTransaction().run {
            add(R.id.fragment, mFragment!!)
            commit()
        }
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
        //报名参加
        tvSingUp.clickN {
            mViewModel.singUpActivity(mAppointmentModel.id.toString())
        }
        //获取到用户信息
        mViewModel.mUserInfoObserver.observe(this, Observer { data ->
            mFragment?.let {
                when (it) {
                    is AppointmentPlayDetailsFragment -> {
                        it.showUserInfo(data)
                    }
                    is AppointmentCarDetailsFragment -> {
                        it.showUserInfo(data)
                    }
                    is AppointmentWorkDetailsFragment -> {
                        it.showUserInfo(data)
                    }
                }
            }

        })

        mViewModel.mSingUpResultObserver.observe(this, Observer {
            if (it) {
                finish()
            }
        })
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