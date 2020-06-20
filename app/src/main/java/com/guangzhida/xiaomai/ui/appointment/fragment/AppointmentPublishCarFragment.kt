package com.guangzhida.xiaomai.ui.appointment.fragment

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.ui.appointment.viewmodel.AppointmentPublishViewModel
import com.guangzhida.xiaomai.ui.home.adapter.PhotoMultipleItem
import com.guangzhida.xiaomai.ui.user.adapter.FeedBackPhotoAdapter
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.SpacesItemDecoration
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import kotlinx.android.synthetic.main.fragment_appointment_publish_car_layout.*
import permissions.dispatcher.ktx.withPermissionsCheck
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*

/**
 * 发布约吗 - 约车
 */
class AppointmentPublishCarFragment : BaseFragment<AppointmentPublishViewModel>() {

    override fun layoutId(): Int = R.layout.fragment_appointment_publish_car_layout

    override fun initListener() {
        rgSelectMoneyType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbSelectMoneyTypeOne -> {
                    etActivityMoney.gone()
                }
                R.id.rbSelectMoneyTypeTwo, R.id.rbSelectMoneyTypeThree, R.id.rbSelectMoneyTypeFour -> {
                    etActivityMoney.visible()
                }
            }
        }
        //选择活动时间
        tvSelectActivityTime.clickN {
            showDateTimePickerDialog(tvSelectActivityTime)
        }
        //选择报名截止时间
        tvSignUpEndTime.clickN {
            showDateTimePickerDialog(tvSignUpEndTime)
        }
        viewModel.mSubmitResultObserver.observe(this, androidx.lifecycle.Observer {
            if (it) {
                LiveDataBus.with(LiveDataBusKey.PUBLISH_APPOINTMENT_FINISH_KEY).postValue(true)
                activity?.finish()
            }
        })
    }

    fun publish() {
        val startAddress = etStartAddressTitle.text.toString().trim()
        val endAddress = etEndAddress.text.toString().trim()
        val money = etActivityMoney.text.toString().trim()
        val startTime = tvSelectActivityTime.text.toString().trim()
        val signUpTime = tvSignUpEndTime.text.toString().trim()
        val boyPeoples = etBoyPeoples.text.toString().trim()
        val moneyType = getMoneyType()
        val carType = getCarType()
        if (startAddress.isEmpty()) {
            ToastUtils.toastShort("请输入起始地点")
            return
        }
        if (endAddress.isEmpty()) {
            ToastUtils.toastShort("请输入到达地点")
            return
        }
        if (moneyType > 0) {
            if (money.isEmpty()) {
                ToastUtils.toastShort("请输入活动经费")
                return
            }
        }
        if (boyPeoples.isEmpty()) {
            ToastUtils.toastShort("请至少输入一个同行人数")
            return
        }
        if (startTime.isEmpty()) {
            ToastUtils.toastShort("请选择活动开始时间")
            return
        }
        if (signUpTime.isEmpty()) {
            ToastUtils.toastShort("请选择报名截止时间")
            return
        }
        viewModel.doSubmit(
            2,
            "",
            "",
            "",
            carType.toString(),
            moneyType,
            money,
            startTime,
            signUpTime,
            boyPeoples,
            "0",
            startAddress,
            endAddress,
            listOf()
        )
    }

    private fun getMoneyType(): Int {
        return when {
            rbSelectMoneyTypeOne.isChecked -> {
                return 0
            }
            rbSelectMoneyTypeTwo.isChecked -> {
                return 1
            }
            rbSelectMoneyTypeThree.isChecked -> {
                return 2
            }
            rbSelectMoneyTypeFour.isChecked -> {
                return 2
            }
            else -> {
                0
            }
        }
    }

    private fun getCarType(): Int {
        return when {
            rbCarTypeOne.isChecked -> {
                return 2
            }
            rbCarTypeTwo.isChecked -> {
                return 1
            }
            rbCarTypeThree.isChecked -> {
                return 3
            }
            else -> {
                1
            }
        }
    }

    /**
     * 展示日期时间选择器
     */
    private fun showDateTimePickerDialog(tv: TextView) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.MONTH, +1);//把月份减三个月
        TimePickerBuilder(context,
            OnTimeSelectListener { date, _ ->
                tv.text = date.time.formatDateTime("yyyy/MM/dd HH:mm")
            })
            .setType(BooleanArray(6) {
                it < 4
            })
            .setSubmitColor(Color.WHITE)//确定按钮文字颜色
            .setCancelColor(Color.WHITE)//取消按钮文字颜色
            .setRangDate(startDate, endDate)//起始终止年月日设定
//          .isCyclic(true)
            .build()
            .show(true)
    }

}