package com.guangzhida.xiaomai.ui.chat

import android.os.Bundle
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.viewmodel.PersonInfoViewModel
import kotlinx.android.synthetic.main.activity_person_info_layout.*

/**
 * 好友信息的界面
 */
class PersonInfoActivity : BaseActivity<PersonInfoViewModel>() {
    private var mState = 0
    private var mUserEntity: UserEntity? = null
    private val mGson by lazy {
        Gson()
    }

    override fun layoutId(): Int = R.layout.activity_person_info_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mState = intent.getIntExtra("State", 0)
        val userEntityGson = intent.getStringExtra("UserEntityGson")
        mUserEntity = mGson.fromJson(userEntityGson, UserEntity::class.java)
        if (mState == 0) {//添加好友
            tvSendMessage.text = "添加好友"
            tvDeleteFriend.gone()
        } else {//已经是好友了
            tvSendMessage.text = "发送消息"
        }
        initUserInfo()
    }

    override fun initListener() {
        //返回
        toolBar.setNavigationOnClickListener {
            finish()
        }
        //点击删除好友
        tvDeleteFriend.clickN {
            mUserEntity?.let {
                mViewModel.deleteFriend(it)
            }
        }
        //点击添加好友或者发送消息
        tvSendMessage.clickN {
            if (mState == 0) {
                //添加好友
                mUserEntity?.let {
                    mViewModel.addFriend(it)
                }
            } else {
                //发送消息
                startKtxActivity<ChatMessageActivity>(
                    value = Pair("userName", mUserEntity?.userName ?: "")
                )
            }
        }
        rlSettingRemark.clickN {
            mUserEntity?.let {
                startKtxActivity<SettingRemarkActivity>(
                    value = Pair("UserEntityGson", mGson.toJson(it))
                )
            }

        }
    }

    /**
     * 注册观察者
     */
    private fun registerLiveDataObserver() {

    }

    /**
     * 初始化
     */
    private fun initUserInfo() {
        tvRemarkName.text = mUserEntity?.remarkName
        //设置头像
        ivHeaderView.loadFilletRectangle(
            BASE_URL.substring(0, BASE_URL.length - 1) + mUserEntity?.avatarUrl,
            holder = R.mipmap.icon_default_header
        )
        tvName.text = mUserEntity?.nickName
        tvAccountId.text = buildString {
            append("账号ID: ")
            append(mUserEntity?.uid)
        }
        val sex = if (mUserEntity?.sex?.toInt() == 1) {
            "男"
        } else {
            "女"
        }
        tvSexAge.text = buildString {
            append(sex)
            append("    ")
            append(mUserEntity?.age)
            append("岁")
        }
        tvSignName.text = mUserEntity?.singUp?:""
    }
}