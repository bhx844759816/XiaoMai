package com.guangzhida.xiaomai.ui.chat.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import kotlinx.android.synthetic.main.fragment_conversation_layout.view.*

/**
 * 会话界面的Adapter
 */
class ConversationAdapter(list: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_conversation_layout, list) {
    override fun convert(helper: BaseViewHolder, item: String) {
    }
}