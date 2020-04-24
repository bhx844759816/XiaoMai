package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.model.SearchMessageModel
import com.guangzhida.xiaomai.room.entity.UserEntity

/**
 * 查询更多聊天记录的适配器
 */
class SearchMessageListAdapter(list: MutableList<SearchMessageModel>) :
    BaseQuickAdapter<SearchMessageModel, BaseViewHolder>(
        R.layout.adapter_search_message_list_item_layout, list
    ) {
    var mContentClickCallBack: ((SearchMessageModel) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: SearchMessageModel) {
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
        val message = buildString {
            append(item.messageCount)
            append("条相关的聊天记录")
        }
        helper.setText(R.id.tvSubTitle, message)
        parent.setOnClickListener {
            mContentClickCallBack?.invoke(item)
        }
    }
}