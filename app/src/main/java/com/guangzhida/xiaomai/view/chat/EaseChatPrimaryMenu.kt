package com.guangzhida.xiaomai.view.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.guangzhida.xiaomai.R

/**
 * 输入框 语音和发送
 */
class EaseChatPrimaryMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    private val mContext = context

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_chat_primary_menu, this)

    }

}