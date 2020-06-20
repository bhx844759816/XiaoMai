package com.guangzhida.xiaomai.ui.appointment.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.formatDateTime
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.visible
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.ui.appointment.viewmodel.AppointmentDetailsViewModel
import kotlinx.android.synthetic.main.fragment_appointment_work_details_layout.*

/**
 * 约吗 - 约工作详情
 */
class AppointmentWorkDetailsFragment : BaseFragment<AppointmentDetailsViewModel>() {
    private var mAppointmentModel: AppointmentModel? = null
    override fun layoutId(): Int = R.layout.fragment_appointment_work_details_layout

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
                    "(日结)"
                }
                2 -> {
                    "(周结)"
                }
                3 -> {
                    "(月结)"
                }
                else -> "(日结)"
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
        ivUserAvatar.loadCircleImage(
            BASE_URL_IMG + "/" + data.headUrl,
            holder = R.mipmap.icon_user_center_header
        )
        tvUserName.text = data.nickName
    }
    companion object {
        fun newInstance(appointmentModel: AppointmentModel): Fragment {
            val fragment = AppointmentWorkDetailsFragment()
            fragment.arguments = Bundle().apply {
                putSerializable("AppointmentModel", appointmentModel)
            }
            return fragment
        }
    }
}