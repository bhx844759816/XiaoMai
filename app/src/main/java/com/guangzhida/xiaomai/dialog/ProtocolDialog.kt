package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.utils.ToastUtils

/**
 * 首页弹出的用户协议和隐私权政策
 */
object ProtocolDialog {

    private var mDialog:MaterialDialog?=null
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callBack1: () -> Unit,
        callBack2: () -> Unit,
        confirmCallBack:()->Unit
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_protocol_layout, null)
        val tvContent = view.findViewById<TextView>(R.id.content)
        val tvConfirm = view.findViewById<TextView>(R.id.confirm)

        val strBuilder1 = SpannableStringBuilder(
            "欢迎您使用校麦!\n\u3000\u3000我们将通过《校麦用户协议》和《隐私权政策》帮助我们收集、使用、" +
                    "储存和共享个人信息的情况，以及您所享有的相关权利。未经您的再次同意,我们不会将上述信息用于您未授权的其他用途或目的。"
        )
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                callBack1.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {

            }
        }
        val clickableSpan2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                callBack2.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {

            }
        }
        val clickableSpan3 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                callBack1.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {

            }
        }
        val clickableSpan4 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                callBack2.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {

            }
        }
        strBuilder1.setSpan(
            ForegroundColorSpan(Color.parseColor("#9245ed")),
            16,
            24,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        strBuilder1.setSpan(clickableSpan1, 16, 24, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        strBuilder1.setSpan(
            ForegroundColorSpan(Color.parseColor("#9245ed")),
            25,
            32,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        strBuilder1.setSpan(clickableSpan2, 25, 32, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        val strBuilder2 =
            SpannableStringBuilder("\u3000\u3000您可以通过阅读完整的《校麦用户协议》和《隐私协议》，了解个人信息类型与用途的对应关系等更加详尽的个人信息处理规则。\n如您同意，请点击“同意”开始接受我们的服务")
        strBuilder2.setSpan(
            ForegroundColorSpan(Color.parseColor("#9245ed")),
            12,
            20,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        strBuilder2.setSpan(
            clickableSpan3,
            12,
            20,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        strBuilder2.setSpan(
            ForegroundColorSpan(Color.parseColor("#9245ed")),
            21,
            28,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        strBuilder2.setSpan(
            clickableSpan4,
            21,
            28,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val content = SpannableStringBuilder()
        content.append(strBuilder1).append("\n").append(strBuilder2)
        tvContent.text = content
        tvContent.movementMethod = LinkMovementMethod.getInstance()
        tvContent.highlightColor = context.resources.getColor(android.R.color.transparent)
        tvConfirm.setOnClickListener {
            confirmCallBack.invoke()
            mDialog?.dismiss()
        }

        MaterialDialog(context)
            .cancelable(false)
            .cornerRadius(8f)
            .customView(view = view, noVerticalPadding = true)
            .show{
                mDialog = this
            }
    }
}