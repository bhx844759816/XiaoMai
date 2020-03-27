package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.model.ChatUserModel
import org.w3c.dom.Text

/**
 * 搜索好友适配器
 *
 */
class AddFriendsAdapter(
    list: MutableList<ChatUserModel>,
    callBack: ((ChatUserModel) -> Unit)? = null
) :
    BaseQuickAdapter<ChatUserModel, BaseViewHolder>(R.layout.adapter_friends_layout, list) {
    private val mCallBack = callBack
    override fun convert(helper: BaseViewHolder, item: ChatUserModel) {
        val ivHeadView = helper.getView<ImageView>(R.id.ivHeadView)
        ivHeadView.loadCircleImage(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.headUrl
        )
        helper.setText(R.id.tvName, item.nickName)
        helper.setText(R.id.tvPhone, "(${item.mobilePhone})")
        helper.getView<TextView>(R.id.tvAddFriend).setOnClickListener {
            mCallBack?.invoke(item)
        }
    }

}