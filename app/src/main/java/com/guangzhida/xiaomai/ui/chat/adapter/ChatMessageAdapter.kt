package com.guangzhida.xiaomai.ui.chat.adapter

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.*
import android.view.textservice.TextInfo
import android.widget.*
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.ext.loadImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.clipboardManager
import com.guangzhida.xiaomai.ktxlibrary.ext.dp2px
import com.guangzhida.xiaomai.ktxlibrary.ext.screenWidth
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.chat.SimpleCommonUtils
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.chat.EMVoiceMessageBody
import com.hyphenate.util.DateUtils
import com.lxj.xpopup.XPopup
import java.io.File
import java.util.*

/**
 * 聊天界面的适配器
 */
class ChatMessageAdapter(
    data: MutableList<ChatMultipleItem>,
    userAvatar: String?,
    context: Context
) :
    BaseMultiItemQuickAdapter<ChatMultipleItem, BaseViewHolder>(data = data) {
    var mUserAvatar: String? = userAvatar //好友的头像
    var mContext: Context = context
    var mImageCallBack: ((ChatMultipleItem) -> Unit)? = null //点击图片跳转到图片浏览
    var mVoiceClickCallBack: ((View, ChatMultipleItem) -> Unit)? = null //点击语音播放
    var mDeleteMessageCallBack: ((ChatMultipleItem) -> Unit)? = null //删除消息
    var mForwardMessageCallback: ((ChatMultipleItem) -> Unit)? = null//转发消息
    var mHeaderClickCallBack: (() -> Unit)? = null //点击头像跳转
    private var maxWidth = (context.screenWidth * 2) / 3
    private var minWidth = context.dp2px(70)

    init {
        addItemType(ChatMultipleItem.LEFT_MESSAGE, R.layout.layout_chat_message_left)//左边视图布局
        addItemType(ChatMultipleItem.RIGHT_MESSAGE, R.layout.layout_chat_message_right)//右边视图布局
    }

    override fun convert(helper: BaseViewHolder, item: ChatMultipleItem) {
        val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
        val timestamp = helper.getView<TextView>(R.id.timestamp)
        val textView = helper.getView<TextView>(R.id.tv_content)
        val ivPicImg = helper.getView<ImageView>(R.id.iv_img) //图片
        val rlVoiceBg = helper.getView<LinearLayout>(R.id.rlVoiceBg) //语音的背景
        val ivVoice = helper.getView<ImageView>(R.id.ivVoice) //语音的图片
        val tvVoice = helper.getView<TextView>(R.id.tvVoice) //语音的文本
        textView.maxWidth = maxWidth
        textView.setOnLongClickListener {
            showPopupMenu2(textView, item)
            return@setOnLongClickListener true
        }
        //
        showTimestampView(item, timestamp)
        //浏览图片
        ivPicImg.setOnClickListener {
            mImageCallBack?.invoke(item)
        }
        //点击语音
        rlVoiceBg.setOnClickListener {
            mVoiceClickCallBack?.invoke(ivVoice, item)
        }
        when (helper.itemViewType) {
            ChatMultipleItem.LEFT_MESSAGE -> {
                showAvatarImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + mUserAvatar, ivAvatar
                )
                //点击头像
                ivAvatar.clickN {
                    mHeaderClickCallBack?.invoke()
                }
                when (item.mMessage.type) {
                    EMMessage.Type.TXT -> showTextMessage(item, ivPicImg, rlVoiceBg, textView)
                    EMMessage.Type.IMAGE -> showImageMessage(item, ivPicImg, rlVoiceBg, textView)
                    EMMessage.Type.VOICE -> showVoiceMessage(
                        item,
                        ivPicImg,
                        rlVoiceBg,
                        textView,
                        ivVoice,
                        tvVoice
                    )
                    else -> {
                    }
                }
            }
            ChatMultipleItem.RIGHT_MESSAGE -> {
                //加载本人头像
                showAvatarImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + BaseApplication.instance().mUserModel?.headUrl, ivAvatar
                )
                when (item.mMessage.type) {
                    EMMessage.Type.TXT -> showTextMessage(item, ivPicImg, rlVoiceBg, textView)
                    EMMessage.Type.IMAGE -> showImageMessage(item, ivPicImg, rlVoiceBg, textView)
                    EMMessage.Type.VOICE -> showVoiceMessage(
                        item,
                        ivPicImg,
                        rlVoiceBg,
                        textView,
                        ivVoice,
                        tvVoice
                    )
                    else -> {

                    }
                }

            }

        }
    }

    /**
     * 展示顶部时间提示
     */
    private fun showTimestampView(item: ChatMultipleItem, timestamp: TextView) {
        val position = data.indexOf(item)
        if (position == data.size - 1) {
            timestamp.text = DateUtils.getTimestampString(
                Date(
                    item.mMessage.msgTime
                )
            )
            timestamp.visibility = View.VISIBLE
        } else {
            val prevMessage = data[position + 1].mMessage
            if (DateUtils.isCloseEnough(item.mMessage.msgTime, prevMessage.msgTime)) {
                timestamp.visibility = View.GONE
            } else {
                timestamp.text = DateUtils.getTimestampString(Date(item.mMessage.msgTime))
                timestamp.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 展示头像
     */
    private fun showAvatarImage(url: String, ivAvatar: ImageView) {
        ivAvatar.loadFilletRectangle(
            url,
            holder = R.mipmap.icon_default_header
        )
    }

    /**
     * 展示文本的Text
     */
    private fun showTextMessage(
        item: ChatMultipleItem,
        ivPicImg: ImageView,
        rlVoiceBg: LinearLayout,
        textView: TextView
    ) {
        val messageBody = item.mMessage.body as EMTextMessageBody
        val content = messageBody.message
        ivPicImg.visibility = View.GONE
        rlVoiceBg.visibility = View.GONE
        textView.visibility = View.VISIBLE
            SimpleCommonUtils.spannableEmoticonFilter(textView, content)
    }



    /**
     * 展示图片信息
     */
    private fun showImageMessage(
        item: ChatMultipleItem,
        ivPicImg: ImageView,
        rlVoiceBg: LinearLayout,
        textView: TextView
    ) {
        ivPicImg.visibility = View.VISIBLE
        rlVoiceBg.visibility = View.GONE
        textView.visibility = View.GONE
        val messageBody = item.mMessage.body as EMImageMessageBody
        if (!TextUtils.isEmpty(messageBody.localUrl) && File(messageBody.localUrl).exists()) {
            ivPicImg.loadFilletRectangle(
                messageBody.localUrl ?: messageBody.remoteUrl, roundingRadius = 15
            )
        } else {
            ivPicImg.loadFilletRectangle(messageBody.remoteUrl, roundingRadius = 15)
        }
    }

    /**
     * 展示录音的Message
     */
    private fun showVoiceMessage(
        item: ChatMultipleItem,
        ivPicImg: ImageView,
        rlVoiceBg: LinearLayout,
        textView: TextView,
        ivVoice: ImageView,
        tvVoice: TextView
    ) {
        ivPicImg.visibility = View.GONE
        rlVoiceBg.visibility = View.VISIBLE
        textView.visibility = View.GONE
        val messageBody = item.mMessage.body as EMVoiceMessageBody
        val time = (messageBody.length / 1000)
        tvVoice.text = buildString {
            append(time)
            append(" ″ ")
        }
        rlVoiceBg.layoutParams = rlVoiceBg.layoutParams.apply {
            val cWidth = minWidth + (time * (maxWidth - minWidth)) / 100
            LogUtils.i("cWidth=$cWidth")
            width = if (cWidth > maxWidth) {
                maxWidth
            } else {
                cWidth
            }
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val animationDrawable = ivVoice.background as AnimationDrawable
        if (item.isVoicePlay) {
            animationDrawable.start()
        } else {
            animationDrawable.stop()
            animationDrawable.selectDrawable(animationDrawable.numberOfFrames - 1)
        }
    }

    /**
     * 显示
     */
    private fun showPopupMenu2(view: TextView, item: ChatMultipleItem) {
        XPopup.Builder(mContext)
            .atView(view)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
            .hasShadowBg(false)
            .isCenterHorizontal(true)
            .asAttachList(arrayOf("复制", "删除"), null) { position, _ ->
                when (position) {
                    0 -> { //复制
                        val content = view.text.toString().trim()
                        context.clipboardManager?.primaryClip = ClipData.newPlainText(null, content)
                        ToastUtils.toastShort("复制成功")
                    }
                    1 -> {//删除
                        mDeleteMessageCallBack?.invoke(item)

                    }
                }
            }
            .show()
    }

}