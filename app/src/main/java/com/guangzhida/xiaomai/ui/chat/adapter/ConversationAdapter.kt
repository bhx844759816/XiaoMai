package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.view.chat.SimpleCommonUtils
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.util.DateUtils
import java.util.*

/**
 * 会话界面的Adapter
 */
class ConversationAdapter(data: MutableList<EMConversation>) :
    BaseQuickAdapter<EMConversation, BaseViewHolder>(R.layout.adapter_conversation_layout, data) {
    override fun convert(helper: BaseViewHolder, item: EMConversation) {
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        val tvName = helper.getView<TextView>(R.id.tvName)
        val tvChatMessage = helper.getView<TextView>(R.id.tvChatMessage)
        val tvTime = helper.getView<TextView>(R.id.tvTime)
        val emMessage = item.lastMessage
        // get username or group id
        val username: String = item.conversationId()
        if (emMessage != null) {
            tvName.text = emMessage.getStringAttribute("UserNickName", "")
            ivHeaderView.loadCircleImage(
                BASE_URL.substring(0, BASE_URL.length - 1)
                        + emMessage.getStringAttribute("UserAvatar", ""),
                holder = R.mipmap.icon_default_header
            )
            if (emMessage.type == EMMessage.Type.TXT) {
                SimpleCommonUtils.spannableEmoticonFilter(
                    tvChatMessage,
                    ((emMessage.body) as EMTextMessageBody).message
                )
            } else {
                tvChatMessage.text = getMessageDigest(emMessage)
            }
            tvTime.text =
                DateUtils.getTimestampString(Date(emMessage.msgTime))
        }
    }


    private fun getMessageDigest(
        message: EMMessage
    ): String? {
        return when (message.type) {
            EMMessage.Type.LOCATION -> "[地址]"
            EMMessage.Type.IMAGE -> "[图片]"
            EMMessage.Type.VOICE -> "[语音]"
            EMMessage.Type.VIDEO -> "[视频]"
            EMMessage.Type.FILE -> "[文件]"
            else -> {
                return ""
            }
        }
    }
}