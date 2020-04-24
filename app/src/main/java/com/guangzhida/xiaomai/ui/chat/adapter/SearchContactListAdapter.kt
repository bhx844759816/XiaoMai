package com.guangzhida.xiaomai.ui.chat.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.room.entity.UserEntity

class SearchContactListAdapter(list: MutableList<UserEntity>) :
    BaseQuickAdapter<UserEntity, BaseViewHolder>(
        R.layout.adapter_search_contact_list_item_layout, list
    ) {
    var mContentClickCallBack: ((UserEntity) -> Unit)? = null
    var mKey:String=""
    override fun convert(helper: BaseViewHolder, item: UserEntity) {
        val parent = helper.getView<ConstraintLayout>(R.id.parent)
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        ivHeaderView.loadFilletRectangle(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.avatarUrl
        )
        val start = item.nickName.indexOf(mKey)
        val end = start + mKey.length
        val spannableString = SpannableString(item.nickName)
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#33CC99")),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        //设置备注或者昵称
        helper.setText(R.id.tvName, spannableString)
        parent.setOnClickListener {
            mContentClickCallBack?.invoke(item)
        }
    }
}