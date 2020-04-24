package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.model.ChatMessageRecordModel
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.hyphenate.util.DateUtils
import java.util.*

class ChatMessageRecordAdapter(list: MutableList<ChatMessageRecordModel>) :
    BaseQuickAdapter<ChatMessageRecordModel, BaseViewHolder>(
        R.layout.adapter_chat_message_record_layout, list
    ) {
    var mContentClickCallBack: ((ChatMessageRecordModel) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: ChatMessageRecordModel) {
        val parent = helper.getView<ConstraintLayout>(R.id.parent)
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        ivHeaderView.loadFilletRectangle(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.userEntity.avatarUrl
        )
        //设置备注或者昵称
        helper.setText(
            R.id.tvName, if (item.userEntity.remarkName.isNotEmpty()) {
                item.userEntity.remarkName
            } else {
                item.userEntity.nickName
            }
        )
        helper.setText(R.id.tvSubTitle, item.message)
        helper.setText(R.id.tvTime, DateUtils.getTimestampString(Date(item.atTime)))
        parent.setOnClickListener {
            mContentClickCallBack?.invoke(item)
        }
    }

}