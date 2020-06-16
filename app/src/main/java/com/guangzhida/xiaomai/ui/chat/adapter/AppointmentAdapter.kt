package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.formatDateTime
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 展示约吗列表
 */
class AppointmentAdapter(list: MutableList<AppointmentModel>) :
    BaseQuickAdapter<AppointmentModel, BaseViewHolder>(
        R.layout.adapter_appointment_item_layout,
        list
    ) {

    var mItemClickCallBack: ((AppointmentModel) -> Unit)? = null
    var mSignUpClickCallBack: ((AppointmentModel) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: AppointmentModel) {
        helper.setText(R.id.tvTitle, item.title)
        helper.setText(R.id.tvDescribe, item.content)
        helper.setText(R.id.tvDate, buildString {
            append("参加日期: ")
            append(item.activityStartTime.formatDateTime("yyyy/MM/dd HH:mm"))
        })
        helper.setText(R.id.tvPeoples, buildString {
            append("人数: ")
            append(item.boyCount)
            append("男,")
            append(item.girlCount)
            append("女")
        })
        val tvMoney = helper.getView<TextView>(R.id.tvMoney)
        val tvSign = helper.getView<TextView>(R.id.tvSign)
        val ivPic = helper.getView<ImageView>(R.id.ivPic)

        if (item.feeType == 0) {
            tvMoney.text = "免费"
        } else {
            val type = when (item.feeType) {
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
            KtxSpan().with(tvMoney).text("$${item.activityMoney}", isNewLine = false)
                .text(type, isNewLine = false, textSize = 12).show { }
        }
        val photoPic = item.activityPic.split(",")[0]
        ivPic.loadFilletRectangle(
            "$BASE_URL$photoPic",
            holder = R.mipmap.icon_img_error_holder_w,
            roundingRadius = 5
        )
        tvSign.clickN {
            mSignUpClickCallBack?.invoke(item)
        }
        helper.getView<ConstraintLayout>(R.id.parent).clickN {
            mItemClickCallBack?.invoke(item)
        }

    }
}