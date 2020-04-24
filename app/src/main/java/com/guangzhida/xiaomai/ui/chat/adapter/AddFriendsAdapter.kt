package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.model.ChatUserModel
import org.w3c.dom.Text

/**
 * 搜索好友适配器
 *
 */
class AddFriendsAdapter(
    list: MutableList<ChatUserModel>
) :
    BaseQuickAdapter<ChatUserModel, BaseViewHolder>(R.layout.adapter_friends_layout, list) {
    var mCallBack :((ChatUserModel)->Unit)?=null
    var mContentClickCallBack :((ChatUserModel)->Unit)?=null
    override fun convert(helper: BaseViewHolder, item: ChatUserModel) {
        val parent = helper.getView<ConstraintLayout>(R.id.parent)
        val ivHeadView = helper.getView<ImageView>(R.id.ivHeadView)
        ivHeadView.loadCircleImage(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.headUrl
        )
        helper.setText(R.id.tvName, item.nickName)
        helper.setText(R.id.tvPhone, "(${item.mobilePhone})")
        helper.getView<TextView>(R.id.tvAddFriend).setOnClickListener {
            mCallBack?.invoke(item)
        }
        //查看联系人信息
        parent.clickN {
            mContentClickCallBack?.invoke(item)
        }
    }

}