package com.guangzhida.xiaomai.dialog

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.Property
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.span.KtxSpan
import com.guangzhida.xiaomai.model.NetworkCheckModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.NetworkUtils

/**
 * 网络诊断Dialog
 */
object NetworkCheckDialog2 {
    private var dialog: MaterialDialog? = null
    private var mAnimator: ValueAnimator? = null
    private var mTvContent: TextView? = null
    /**
     *
     */
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        dismissCallBack: (() -> Unit)? = null
    ) {
        var isWifi = false
        val tips = when {
            NetworkUtils.isWifiConnected(context) -> {
                isWifi = true
                "提示: 当前数据连接类型为WIFI"
            }
            NetworkUtils.is4G(context) -> {
                "提示: 当前数据连接类型为4G"
            }
            else -> {
                "提示: 当前数据连接类型未知"
            }
        }
        val wifiName = buildString {
            append("WIFI名称: ")
            append(NetworkUtils.getWifiName(context))
        }

        val macName = buildString {
            append("接入点MAC: ")
            append(NetworkUtils.getWifiMAC(context) ?: NetworkUtils.getMac())
        }
        MaterialDialog(context)
            .cancelable(true)
            .cornerRadius(8f)
            .customView(viewRes = R.layout.dialog_network_check_layout)
            .lifecycleOwner(owner)
            .show {
                val tvConnectTips = getCustomView().findViewById<TextView>(R.id.tvConnectTips)
                val tvWifiName = getCustomView().findViewById<TextView>(R.id.tvWifiName)
                val tvMacName = getCustomView().findViewById<TextView>(R.id.tvMacName)
                mTvContent = getCustomView().findViewById<TextView>(R.id.tvContent)
                tvConnectTips.text = tips
                tvWifiName.visibility = if (isWifi) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                if (isWifi)
                    tvWifiName.text = wifiName
                tvMacName.text = macName
            }.setOnDismissListener {
                mAnimator?.cancel()
                mAnimator = null
                dismissCallBack?.invoke()
            }
    }

    /**
     * 设置检测结果
     */
    fun setNetworkCheckContent(networkCheckModel: NetworkCheckModel) {
        mTvContent?.let {
            LogUtils.i("setNetworkCheckContent")
            val ktxSpan = KtxSpan().with(it)
            ktxSpan.text(networkCheckModel.content)
            if (networkCheckModel.checkSuccess) {
                ktxSpan.text("检测网络正常", foregroundColor = Color.parseColor("#00ff00"), textSize = 18)
                    .show { }//设置
            } else {
                ktxSpan.text("检测网络异常", foregroundColor = Color.parseColor("#ff0000"), textSize = 18)
                    .show { }//设置
            }

        }
    }

    /**
     * 展示加载中动画
     */
    private fun showLoadAnim(textView: TextView) {
        mAnimator?.cancel()
        mAnimator = ValueAnimator.ofInt(0, 3)
        mAnimator?.repeatCount = ValueAnimator.INFINITE
        mAnimator?.interpolator = LinearInterpolator()
        mAnimator?.duration = 3000
        mAnimator?.addUpdateListener {
            val text = when (it.animatedValue) {
                0 -> {
                    "加载中."
                }
                1 -> {
                    "加载中.."
                }
                2 -> {
                    "加载中..."
                }
                else -> "加载中"
            }
            LogUtils.i("animatedValue = ${it.animatedValue}")
            textView.text = text
        }
        mAnimator?.start()
    }
}