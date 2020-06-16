package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.http.BASE_URL_IMG
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.model.ChatUserModel

/**
 * 报名约吗的item适配器
 */
class AppointmentSingUpAdapter(list: MutableList<ChatUserModel>) :
    BaseQuickAdapter<ChatUserModel, BaseViewHolder>(
        R.layout.adapter_appointment_signup_item_layout,
        list
    ) {
    var mContactCallBack: ((ChatUserModel) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: ChatUserModel) {
        val ivUserAvatar = helper.getView<ImageView>(R.id.ivUserAvatar)
        helper.setText(R.id.tvUserName, item.nickName)
        ivUserAvatar.loadFilletRectangle(
            BASE_URL + item.headUrl,
            R.mipmap.icon_default_header
        )
        //联系他
        helper.getView<TextView>(R.id.tvContact).clickN {
            mContactCallBack?.invoke(item)
        }
    }
}