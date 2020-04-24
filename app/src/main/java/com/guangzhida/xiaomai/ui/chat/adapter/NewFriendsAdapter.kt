package com.guangzhida.xiaomai.ui.chat.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.visible
import com.guangzhida.xiaomai.model.ChatUserModel
import com.guangzhida.xiaomai.model.NewFriendModel
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity

/**
 * 新朋友适配器
 */
class NewFriendsAdapter(
    list: MutableList<NewFriendModel>
) :
    BaseQuickAdapter<NewFriendModel, BaseViewHolder>(
        R.layout.adapter_new_friends_layout,
        list
    ) {
    var mAgreeCallBack: ((NewFriendModel) -> Unit)? = null
    var mItemClickCallBack: ((NewFriendModel) -> Unit)? = null
    var isShowThreeDayItem = true

    override fun convert(helper: BaseViewHolder, item: NewFriendModel) {
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        val parent = helper.getView<ConstraintLayout>(R.id.parent)
        val tvTitle = helper.getView<TextView>(R.id.tvTitle)
        val tvName = helper.getView<TextView>(R.id.tvName)
        val tvState = helper.getView<TextView>(R.id.tvState)
        val tvAgree = helper.getView<TextView>(R.id.tvAgree)
        if (data.indexOf(item) == 0) {
            tvTitle.visible()
            tvTitle.text = "好友通知"
        } else {
            val curItemTime = System.currentTimeMillis() - item.inviteMessageEntity.time
            if (curItemTime > 3 * 24 * 60 * 60 * 1000 && isShowThreeDayItem) {
                tvTitle.visible()
                tvTitle.text = "三天前"
                isShowThreeDayItem = false
            } else {
                tvTitle.gone()
            }
        }
        when (item.inviteMessageEntity.state) {
            0 -> {
                //收到好友请求的时间和当前时间差多少天 7天就过期了
                val curTime = System.currentTimeMillis() - item.inviteMessageEntity.time
                if (curTime >= 7 * 24 * 60 * 60 * 1000) {
                    tvAgree.gone()
                    tvState.visible()
                    tvState.text = "已过期"
                } else {
                    tvAgree.visible()
                    tvState.gone()
                }
            }
            1 -> {//发送的好友请求
                tvAgree.gone()
                tvState.visible()
                tvState.text = "待验证"
            }
            2 -> {//好友请求已同意
                tvAgree.gone()
                tvState.visible()
                tvState.text = "已添加"
            }
            3 -> {//好友请求已拒绝

            }
        }
        ivHeaderView.loadFilletRectangle(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.userEntity.avatarUrl,
            holder = R.mipmap.icon_default_header
        )
        tvName.text = item.userEntity.nickName
        //同意
        tvAgree.clickN {
            mAgreeCallBack?.invoke(item)
        }
        //点击item
        parent.clickN {
            mItemClickCallBack?.invoke(item)
        }
    }


}