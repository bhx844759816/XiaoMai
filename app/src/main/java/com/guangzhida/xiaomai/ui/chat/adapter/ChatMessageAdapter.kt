package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.view.chat.SimpleCommonUtils
import com.hyphenate.chat.EMTextMessageBody

/**
 * 聊天界面的适配器
 */
class ChatMessageAdapter(data: MutableList<ChatMultipleItem>, userAvatar: String?) :
    BaseMultiItemQuickAdapter<ChatMultipleItem, BaseViewHolder>(data = data) {
    private val mUserAvatar = userAvatar

    init {
        addItemType(ChatMultipleItem.LEFT_MESSAGE, R.layout.layout_chat_message_left)//左边视图布局
        addItemType(ChatMultipleItem.RIGHT_MESSAGE, R.layout.layout_chat_message_right)//左边视图布局

    }

    override fun convert(helper: BaseViewHolder, item: ChatMultipleItem) {
        val messageBody = item.mMessage.body as EMTextMessageBody
        val content = messageBody.message
        when (helper.itemViewType) {
            ChatMultipleItem.LEFT_MESSAGE -> {
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                val textView = helper.getView<TextView>(R.id.tv_content)
                SimpleCommonUtils.spannableEmoticonFilter(textView, content)
                ivAvatar.loadCircleImage(
                    BASE_URL.substring(0, BASE_URL.length - 1) + mUserAvatar,
                    holder = R.mipmap.icon_default_header
                )
            }
            ChatMultipleItem.RIGHT_MESSAGE -> {
                val textView = helper.getView<TextView>(R.id.tv_content)
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                SimpleCommonUtils.spannableEmoticonFilter(textView, content)
                ivAvatar.loadCircleImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + BaseApplication.instance().userModel?.headUrl,
                    holder = R.mipmap.icon_default_header
                )
            }

        }
    }
}