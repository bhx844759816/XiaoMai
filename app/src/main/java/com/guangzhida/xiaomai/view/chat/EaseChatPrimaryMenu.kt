package com.guangzhida.xiaomai.view.chat

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.addTextChangedListener
import kotlinx.android.synthetic.main.layout_chat_primary_menu.view.*

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
        //点击声音按钮
        btn_set_mode_voice.setOnClickListener {

        }
        //点击键盘按钮
        btn_set_mode_keyboard.setOnClickListener {

        }
        //按住说话
        btn_press_to_speak.setOnTouchListener { v, event ->

            false
        }
        //点击表情按钮 - 正常状态
        iv_face_normal.setOnClickListener {

        }
        //点击表情按钮 - 选中状态
        iv_face_checked.setOnClickListener {

        }
        //点击+号
        btn_more.setOnClickListener {

        }
        //发送按钮
        btn_send.setOnClickListener {

        }
        //监听输入框
        et_sendmessage.addTextChangedListener {
            afterTextChanged {
                val text = et_sendmessage.text.toString().trim()
                if (text.isEmpty()) {
//                    buttonMore.setVisibility(View.GONE)
//                    buttonSend.setVisibility(View.VISIBLE)
                } else {
//                    buttonMore.setVisibility(View.VISIBLE)
//                    buttonSend.setVisibility(View.GONE)
                }
            }
        }

    }

}