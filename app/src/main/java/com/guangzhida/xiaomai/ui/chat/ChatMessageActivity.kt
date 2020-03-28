package com.guangzhida.xiaomai.ui.chat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ui.chat.adapter.SimpleAppsGridView
import com.sj.emoji.DefEmoticons
import github.ll.emotionboard.adpater.EmoticonPacksAdapter
import github.ll.emotionboard.data.Emoticon
import github.ll.emotionboard.data.EmoticonPack
import github.ll.emotionboard.interfaces.OnEmoticonClickListener
import github.ll.emotionboard.utils.EmoticonsKeyboardUtils
import github.ll.emotionboard.utils.getResourceUri
import github.ll.emotionboard.widget.FuncLayout
import kotlinx.android.synthetic.main.activity_chat_message.*

/**
 * 聊天界面
 */
class ChatMessageActivity : AppCompatActivity(), FuncLayout.FuncKeyBoardListener {

    private val mEmojiListener = OnEmoticonClickListener<Emoticon> {
            if (it is DeleteEmoticon) {
                SimpleCommonUtils.delClick(emoticonsBoard.etChat)
            } else if (it is PlaceHoldEmoticon) { // do nothing
            } else if (it is BigEmoticon) {
//                sendImage(emoticon.uri)
            } else {
                val content = it.code
                if (!TextUtils.isEmpty(content)) {
                    val index: Int = emoticonsBoard.etChat.selectionStart
                    val editable: Editable = emoticonsBoard.etChat.text
                    editable.insert(index, content)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_message)
        init()

    }

    fun getEmoji(context: Context): EmoticonPack<Emoticon> {
        val emojiArray = mutableListOf<Emoticon>()
        DefEmoticons.sEmojiArray.take(60).mapTo(emojiArray) {
            val emoticon = Emoticon()
            emoticon.code = it.emoji
            emoticon.uri = context.getResourceUri(it.icon)
            return@mapTo emoticon
        }
        val pack = EmoticonPack<Emoticon>()
        pack.emoticons = emojiArray
        pack.iconUri = context.getResourceUri(R.drawable.icon_emoji)
        val factory = DeleteBtnPageFactory<Emoticon>()
        factory.deleteIconUri = context.getResourceUri(R.drawable.icon_del)
        factory.line = 3
        factory.row = 7
        pack.pageFactory = factory
        return pack
    }

    fun init() {
        val list = getEmoji(this)
        val packs = mutableListOf<EmoticonPack<Emoticon>>();
        packs.add(list)
        val adapter = EmoticonPacksAdapter(packs)
        emoticonsBoard.setAdapter(adapter)
        emoticonsBoard.addOnFuncKeyBoardListener(this)
        emoticonsBoard.addFuncView(SimpleAppsGridView(this))
        adapter.clickListener = mEmojiListener
        emoticonsBoard.etChat
            .setOnSizeChangedListener { _, _, _, _ -> scrollToBottom() }
        emoticonsBoard.btnSend.setOnClickListener {
            sendBtnClick(emoticonsBoard.etChat.text.toString())
            emoticonsBoard.etChat.setText("")
        }
//        emoticonsBoard.btnVoice.setOnTouchListener { v, event ->
//            return@setOnTouchListener  true
//        }
//        emoticonsBoard.btnVoice.setOnFocusChangeListener { v, hasFocus ->  }
//        emoticonsBoard.btnVoice.setOnClickListener {  }
        val width =
            resources.getDimension(github.ll.emotionboard.R.dimen.bar_tool_btn_width).toInt()
        val leftView = LayoutInflater.from(this)
            .inflate(github.ll.emotionboard.R.layout.left_toolbtn, null)
        var iv_icon =
            leftView.findViewById<View>(github.ll.emotionboard.R.id.iv_icon) as ImageView
        val imgParams =
            LinearLayout.LayoutParams(
                width,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        iv_icon.layoutParams = imgParams
        iv_icon.setImageResource(R.mipmap.icon_add)
        leftView.setOnClickListener {
            Toast.makeText(
                this,
                "添加",
                Toast.LENGTH_SHORT
            ).show()
        }

        emoticonsBoard.emoticonsToolBarView.addFixedToolItemView(leftView, false)

        val rightView = LayoutInflater.from(this)
            .inflate(github.ll.emotionboard.R.layout.right_toolbtn, null)
        iv_icon =
            rightView.findViewById<View>(github.ll.emotionboard.R.id.iv_icon) as ImageView
        iv_icon.setImageResource(R.mipmap.icon_setting)
        iv_icon.layoutParams = imgParams
        rightView.setOnClickListener {
            Toast.makeText(
                this,
                "设置",
                Toast.LENGTH_SHORT
            ).show()
        }
        emoticonsBoard.emoticonsToolBarView.addFixedToolItemView(rightView, true)
    }

    override fun onFuncClose() {
    }

    override fun onFuncPop(height: Int) {
    }

    fun scrollToBottom() {
        recyclerView.requestLayout()
        recyclerView.post({
            //滚动到底部

        })
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (EmoticonsKeyboardUtils.isFullScreen(this)) {
            if (emoticonsBoard.dispatchKeyEventInFullScreen(event)) {
                true
            } else {
                super.dispatchKeyEvent(event)
            }
        } else super.dispatchKeyEvent(event)
    }

    fun sendBtnClick(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
//            val bean = ImMsgBean()
//            bean.setMsgType(ImMsgBean.CHAT_MSGTYPE_TEXT)
//            bean.setContent(msg)
//            sendMsg(bean)
        }
    }

    override fun onPause() {
        super.onPause()
        emoticonsBoard.reset()
    }

}