package com.guangzhida.xiaomai.ui.appointment.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.ui.appointment.viewmodel.AppointmentDetailsViewModel
import com.guangzhida.xiaomai.ui.chat.ChatMessageActivity
import kotlinx.android.synthetic.main.fragment_appointment_play_details_layout.*

/**
 * 约吗详情页-约玩
 */
class AppointmentPlayDetailsFragment : BaseFragment<AppointmentDetailsViewModel>() {
    private var mAppointmentModel: AppointmentModel? = null
    private var mUserModel: UserModel.Data? = null
    override fun layoutId(): Int = R.layout.fragment_appointment_play_details_layout

    override fun initView(savedInstanceState: Bundle?) {
        mAppointmentModel = arguments?.getSerializable("AppointmentModel") as AppointmentModel
        tvTitle.text = mAppointmentModel?.title
        tvDec.text = mAppointmentModel?.content
        tvActivityDetails.text = buildString {
            append("工作日期: ")
            append(mAppointmentModel?.activityStartTime?.formatDateTime())
            append("\n")
            append("工作地址: ")
            append(mAppointmentModel?.activityAddress)
            append("\n")
            append("参与人数: ")
            append(mAppointmentModel?.boyCount)
            append("男,")
            append(mAppointmentModel?.girlCount)
            append("女")
        }
        tvActivitySignUpEndTime.text = mAppointmentModel?.signEndTime?.formatDateTime()
        if (mAppointmentModel?.feeType == 0) {
            tvMoney.text = "免费"
        } else {
            val type = when (mAppointmentModel?.feeType) {
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
            KtxSpan().with(tvMoney).text("$${mAppointmentModel?.activityMoney}", isNewLine = false)
                .text(type, isNewLine = false, textSize = 12).show { }
        }
        if (mAppointmentModel?.userId.toString() == BaseApplication.instance().mUserModel?.id) {
            tvContact.gone()
            tvMoney.visible()
        } else {
            tvContact.visible()
            tvMoney.gone()
        }

    }

    fun showUserInfo(data: UserModel.Data) {
        mUserModel = data
        ivUserAvatar.loadCircleImage(
            BASE_URL_IMG + "/" + data.headUrl,
            holder = R.mipmap.icon_user_center_header
        )
        tvUserName.text = data.nickName
    }

    override fun initListener() {
        tvContact.clickN {
            mUserModel?.let {
                val params = Pair("userName", it.mobilePhone)
                startKtxActivity<ChatMessageActivity>(value = params)
            }
        }
    }

    companion object {
        fun newInstance(appointmentModel: AppointmentModel): Fragment {
            val fragment = AppointmentPlayDetailsFragment()
            fragment.arguments = Bundle().apply {
                putSerializable("AppointmentModel", appointmentModel)
            }
            return fragment
        }
    }
}