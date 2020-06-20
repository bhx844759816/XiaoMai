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
import kotlinx.android.synthetic.main.fragment_appointment_car_details_layout.*

/**
 * 约吗 - 约车详情
 */
class AppointmentCarDetailsFragment : BaseFragment<AppointmentDetailsViewModel>() {
    private var mAppointmentModel: AppointmentModel? = null
    override fun layoutId(): Int = R.layout.fragment_appointment_car_details_layout

    override fun initView(savedInstanceState: Bundle?) {
        mAppointmentModel = arguments?.getSerializable("AppointmentModel") as AppointmentModel
        mAppointmentModel?.let {
            startAddress.text = it.startAddress
            endAddress.text = it.endAddress
            tvActivityTime.text = buildString {
                append("出发时间: ")
                append(it.activityStartTime.formatDateTime())
            }
            tvActivityPeople.text = buildString {
                append("人数: ")
                append(it.boyCount)
                append("人")
            }
            if (it.feeType == 0) {
                tvMoney.text = "免费"
            } else {
                val type = when (it.feeType) {
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
                KtxSpan().with(tvMoney).text("$${it.activityMoney}", isNewLine = false)
                    .text(type, isNewLine = false, textSize = 12).show { }
            }
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
            val fragment = AppointmentCarDetailsFragment()
            fragment.arguments = Bundle().apply {
                putSerializable("AppointmentModel", appointmentModel)
            }
            return fragment
        }
    }
}