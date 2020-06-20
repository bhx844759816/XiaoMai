package com.guangzhida.xiaomai.ui.appointment.adapter

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
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_CAR
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_PLAY
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem.Companion.APPOINTMENT_WORK

class AppointmentAdapter(data: MutableList<AppointmentMultipleItem>) :
    BaseMultiItemQuickAdapter<AppointmentMultipleItem, BaseViewHolder>(data) {
    init {
        addItemType(
            APPOINTMENT_PLAY,
            R.layout.adapter_appointment_item_layout
        )
        addItemType(
            APPOINTMENT_CAR,
            R.layout.adapter_appointment_car_item_layout
        )
        addItemType(
            APPOINTMENT_WORK,
            R.layout.adapter_appointment_item_work_layout
        )
    }

    var mItemClickCallBack: ((AppointmentModel) -> Unit)? = null
    var mSignUpClickCallBack: ((AppointmentModel) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: AppointmentMultipleItem) {
        val data = item.item

        val tvSign = helper.getView<TextView>(R.id.tvSign)
        val tvMoney = helper.getView<TextView>(R.id.tvMoney)

        tvSign.clickN {
            mSignUpClickCallBack?.invoke(data)
        }
        helper.getView<ConstraintLayout>(R.id.parent).clickN {
            mItemClickCallBack?.invoke(data)
        }

        when (item.itemType) {
            APPOINTMENT_PLAY, APPOINTMENT_WORK -> {//约玩
                helper.setText(R.id.tvTitle, data.title)
                helper.setText(R.id.tvDescribe, data.content)
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
                helper.setText(R.id.tvDate, buildString {
                    append("参加日期: ")
                    append(data.activityStartTime.formatDateTime("yyyy/MM/dd HH:mm"))
                })
                helper.setText(R.id.tvPeoples, buildString {
                    append("人数: ")
                    append(data.boyCount)
                    append("男,")
                    append(data.girlCount)
                    append("女")
                })
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
            APPOINTMENT_CAR -> {//约车
                helper.setText(R.id.tvLeaveTime, buildString {
                    append("出发时间: ")
                    append(data.activityStartTime.formatDateTime("MM月dd HH:mm"))
                })
                helper.setText(R.id.tvStartAddress, data.startAddress)
                helper.setText(R.id.tvArrivalAddress, data.endAddress)
                helper.setText(R.id.tvPeoples, buildString {
                    append("人数：")
                    append(data.boyCount)
                    append("人")
                })
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

            }
        }
    }
}