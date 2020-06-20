package com.guangzhida.xiaomai.ui.user.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.formatDateTime
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.visible
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_CAR
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_PLAY
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_WORK

class MyPublishAppointmentAdapter(data: MutableList<AppointmentMultipleItem>) :
    BaseMultiItemQuickAdapter<AppointmentMultipleItem, BaseViewHolder>(data) {


    init {
        addItemType(APPOINTMENT_PLAY, R.layout.adapter_my_publish_appointment_item_layout)
        addItemType(APPOINTMENT_CAR, R.layout.adapter_my_publish_appointment_item_car_layout)
        addItemType(APPOINTMENT_WORK, R.layout.adapter_my_publish_appointment_item_work_layout)
    }

    var mItemClickCallBack: ((AppointmentModel) -> Unit)? = null
    var mItemCheckCallBack: ((AppointmentModel, Boolean) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: AppointmentMultipleItem) {
        val tvMoney = helper.getView<TextView>(R.id.tvMoney)
        val tvSign = helper.getView<TextView>(R.id.tvSign)
        val cbMarquee = helper.getView<CheckBox>(R.id.cbMarquee)
        val layer = helper.getView<View>(R.id.layer)
        val data = item.item

        when (item.itemType) {
            APPOINTMENT_PLAY, APPOINTMENT_WORK -> {
                helper.setText(R.id.tvTitle, data.title)
                helper.setText(R.id.tvDescribe, data.content)
                helper.setText(R.id.tvDate, buildString {
                    append("参加日期: ")
                    append(data.activityStartTime.formatDateTime("MM月dd HH:mm"))
                })
                helper.setText(R.id.tvPeoples, buildString {
                    append("人数: ")
                    append(data.boyCount)
                    append("男,")
                    append(data.girlCount)
                    append("女")
                })
                val ivPic = helper.getView<ImageView>(R.id.ivPic)
                val photoPic = if (data.activityPic != null && data.activityPic.isNotEmpty()) {
                    data.activityPic.split(",")[0]
                } else {
                    ""
                }
                ivPic.loadFilletRectangle(
                    "$BASE_URL$photoPic",
                    holder = R.mipmap.icon_img_error_holder_w,
                    roundingRadius = 5
                )
                if (data.type == 1) {
                    if (data.feeType == 0) {
                        tvMoney.text = "免费"
                    } else {
                        val type = when (data.feeType) {
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
                        KtxSpan().with(tvMoney).text("￥${data.activityMoney}", isNewLine = false)
                            .text(type, isNewLine = false, textSize = 12).show { }
                    }
                } else {
                    val type = when (data.feeType) {
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
                    KtxSpan().with(tvMoney).text("￥${data.activityMoney}", isNewLine = false)
                        .text(type, isNewLine = false, textSize = 12).show { }
                }
            }
            APPOINTMENT_CAR -> {
                val tvLeaveTime = helper.getView<TextView>(R.id.tvLeaveTime)
                val tvPeoples = helper.getView<TextView>(R.id.tvPeoples)
                val tvStartAddress = helper.getView<TextView>(R.id.tvStartAddress)
                val tvEndAddress = helper.getView<TextView>(R.id.tvArrivalAddress)
                tvStartAddress.text = data.startAddress
                tvEndAddress.text = data.endAddress
                tvPeoples.text = buildString {
                    append("人数：")
                    append(data.boyCount)
                    append("人")
                }
                val carType = when (data.walkType) {
                    1 -> {
                        "公共大巴"
                    }
                    2 -> {
                        "出租车"
                    }
                    3 -> {
                        "电动车"
                    }
                    else -> {
                        "大巴"
                    }
                }
                helper.setText(R.id.tvCarType, carType)
                tvLeaveTime.text = buildString {
                    append("出发时间：")
                    append(data.activityStartTime.formatDateTime("MM月dd HH:mm"))
                }
                if (data.feeType == 0) {
                    tvMoney.text = "免费"
                } else {
                    val type = when (data.feeType) {
                        1 -> {
                            "(A费)"
                        }
                        2 -> {
                            "(男A)"
                        }
                        3 -> {
                            "(女A)"

                        }
                        else -> "(A费)"
                    }
                    KtxSpan().with(tvMoney).text("￥${data.activityMoney}", isNewLine = false)
                        .text(type, isNewLine = false, textSize = 12).show { }
                }
            }
        }
        if (data.isEdit) {
            //没有过期且没有人报名的
            cbMarquee.visible()
            if (data.count > 0 && data.isExpire == 0) {
                cbMarquee.visibility = View.INVISIBLE
                layer.gone()
            } else {
                if (data.isChecked) {
                    layer.visible()
                } else {
                    layer.gone()
                }
            }
        } else {
            cbMarquee.gone()
            layer.gone()
        }
        cbMarquee.setOnCheckedChangeListener { _, isChecked ->
            data.isChecked = isChecked
            mItemCheckCallBack?.invoke(data, isChecked)
            if (data.count > 0 && data.isExpire == 0) {
                cbMarquee.visibility = View.INVISIBLE
                layer.gone()
            } else {
                if (isChecked) {
                    layer.visible()
                } else {
                    layer.gone()
                }
            }
        }
        if (data.isExpire == 1) {
            tvSign.text = "活动已结束"
        } else {
            tvSign.text = buildString {
                append("已报名参加")
                append(data.count)
                append("人")
            }
        }
        cbMarquee.isChecked = data.isChecked
        helper.getView<ConstraintLayout>(R.id.parent).clickN {
            if (!data.isEdit) {
                mItemClickCallBack?.invoke(data)
            }
        }
    }
}