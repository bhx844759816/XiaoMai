package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.room.entity.UserEntity

/**
 * 联系人列表
 */
class ContactListAdapter(list: MutableList<UserEntity>) :
    BaseQuickAdapter<UserEntity, BaseViewHolder>(
        R.layout.adapter_contact_list_layout, list
    ) {
    override fun convert(helper: BaseViewHolder, item: UserEntity) {
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        ivHeaderView.loadCircleImage(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.avatarUrl
        )
        helper.setText(R.id.tvName, item.nickName)
    }
}